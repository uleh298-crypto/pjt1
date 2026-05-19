import SwiftUI
import FirebaseCore
import FirebaseMessaging
import UserNotifications

// MARK: - App Delegate

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // Firebase 초기화
        FirebaseApp.configure()

        // 알림 권한 요청 및 설정
        setupNotifications(application: application)

        return true
    }

    // MARK: - Notification Setup

    private func setupNotifications(application: UIApplication) {
        // UNUserNotificationCenter delegate 설정
        UNUserNotificationCenter.current().delegate = self

        // Firebase Messaging delegate 설정
        Messaging.messaging().delegate = self

        // 알림 권한 요청
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { granted, error in
            if let error = error {
                print("[AppDelegate] Notification authorization error: \(error.localizedDescription)")
                return
            }

            if granted {
                print("[AppDelegate] Notification permission granted")
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            } else {
                print("[AppDelegate] Notification permission denied")
            }
        }
    }

    // MARK: - APNs Token Registration

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // APNs 토큰을 Firebase에 전달
        Messaging.messaging().apnsToken = deviceToken

        let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("[AppDelegate] APNs token registered: \(tokenString.prefix(20))...")

        // APNs 토큰이 등록된 후 FCM 토큰 동기화
        // (MessagingDelegate의 didReceiveRegistrationToken이 자동 호출됨)
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("[AppDelegate] Failed to register for remote notifications: \(error.localizedDescription)")
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension AppDelegate: UNUserNotificationCenterDelegate {
    /// 앱이 포그라운드에 있을 때 알림 수신
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        print("[AppDelegate] Foreground notification received: \(userInfo)")

        // 채팅 알림인데 해당 채팅방에 이미 있으면 알림 억제
        if let messageType = userInfo["type"] as? String {
            let type = messageType.uppercased()
            if type == "CHAT_NEW" || type == "MESSAGE" {
                if let roomIdString = userInfo["roomId"] as? String,
                   let roomId = Int(roomIdString),
                   AppForegroundTracker.shared.isForegroundValue,
                   ChatRoomPresence.shared.activeRoomIdValue == roomId {
                    completionHandler([])
                    return
                }
            }
        }

        // 알림 배너만 표시, 자동 네비게이션 없음 (탭 시 didReceive에서 처리)
        completionHandler([.banner, .sound, .badge])
    }

    /// 사용자가 알림을 탭했을 때
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        print("[AppDelegate] Notification tapped: \(userInfo)")

        // 딥링크 처리
        handleNotificationTap(userInfo)

        completionHandler()
    }

    // MARK: - Data Message Handling

    /// 데이터 메시지 처리 - Android의 handleDataMessage와 유사
    /// - Returns: 알림을 억제해야 하면 true
    @discardableResult
    private func handleDataMessage(_ userInfo: [AnyHashable: Any], messageType: String, showNotification: Bool) -> Bool {
        let type = messageType.uppercased()

        switch type {
        case "CHAT_NEW", "MESSAGE":
            return handleChatNew(userInfo, showNotification: showNotification)
        case "POST_COMMENT", "COMMENT_REPLY", "COMMENT", "REPLY":
            return handlePostNotification(userInfo, showNotification: showNotification)
        case "APPLICATION_NEW", "GROUP_APPLICATION", "NEW_APPLICANT", "APPLICANT":
            return handleGroupApplication(userInfo, showNotification: showNotification)
        case "APPLICATION_ACCEPTED", "ACCEPTED", "JOIN_ACCEPTED":
            return handleApplicationAccepted(userInfo, showNotification: showNotification)
        case "APPLICATION_REJECTED", "REJECTED":
            // 거절 알림은 단순 표시
            return false
        default:
            return false
        }
    }

    /// 채팅 알림 처리
    private func handleChatNew(_ userInfo: [AnyHashable: Any], showNotification: Bool) -> Bool {
        guard let roomIdString = userInfo["roomId"] as? String,
              let roomId = Int(roomIdString) else { return false }

        // 포그라운드 + 해당 채팅방이 열려있으면 알림 억제
        if AppForegroundTracker.shared.isForegroundValue,
           ChatRoomPresence.shared.activeRoomIdValue == roomId {
            print("[AppDelegate] Suppressing chat notification for room \(roomId)")
            return true
        }

        // 앱 내 이벤트 전파
        PushEventBus.shared.send(.openChat(roomId: roomId))
        return false
    }

    /// 게시글 댓글 알림 처리
    private func handlePostNotification(_ userInfo: [AnyHashable: Any], showNotification: Bool) -> Bool {
        guard let postIdString = userInfo["postId"] as? String,
              let postId = Int(postIdString) else { return false }

        PushEventBus.shared.send(.openPost(postId: postId))
        return false
    }

    /// 그룹 신청 알림 처리
    private func handleGroupApplication(_ userInfo: [AnyHashable: Any], showNotification: Bool) -> Bool {
        guard let groupIdString = userInfo["groupId"] as? String,
              let groupId = Int(groupIdString),
              let groupType = userInfo["groupType"] as? String else { return false }

        PushEventBus.shared.send(.openGroupApplication(groupId: groupId, groupType: groupType))
        return false
    }

    /// 신청 수락 알림 처리
    private func handleApplicationAccepted(_ userInfo: [AnyHashable: Any], showNotification: Bool) -> Bool {
        guard let groupIdString = userInfo["groupId"] as? String,
              let groupId = Int(groupIdString),
              let groupType = userInfo["groupType"] as? String else { return false }

        PushEventBus.shared.send(.openApplicationAccepted(groupId: groupId, groupType: groupType))
        return false
    }

    /// 알림 탭 시 딥링크 처리
    private func handleNotificationTap(_ userInfo: [AnyHashable: Any]) {
        guard let type = userInfo["type"] as? String else { return }

        switch type.uppercased() {
        case "CHAT_NEW", "MESSAGE":
            if let roomIdString = userInfo["roomId"] as? String,
               let roomId = Int(roomIdString) {
                PushEventBus.shared.send(.openChat(roomId: roomId))
            }
        case "POST_COMMENT", "COMMENT_REPLY", "COMMENT", "REPLY":
            if let postIdString = userInfo["postId"] as? String,
               let postId = Int(postIdString) {
                PushEventBus.shared.send(.openPost(postId: postId))
            }
        case "APPLICATION_NEW", "GROUP_APPLICATION", "NEW_APPLICANT", "APPLICANT":
            if let groupIdString = userInfo["groupId"] as? String,
               let groupId = Int(groupIdString),
               let groupType = userInfo["groupType"] as? String {
                PushEventBus.shared.send(.openGroupApplication(groupId: groupId, groupType: groupType))
            }
        case "APPLICATION_ACCEPTED", "ACCEPTED", "JOIN_ACCEPTED":
            if let groupIdString = userInfo["groupId"] as? String,
               let groupId = Int(groupIdString),
               let groupType = userInfo["groupType"] as? String {
                PushEventBus.shared.send(.openApplicationAccepted(groupId: groupId, groupType: groupType))
            }
        default:
            break
        }
    }
}

// MARK: - MessagingDelegate

extension AppDelegate: MessagingDelegate {
    /// FCM 토큰이 갱신되었을 때 호출
    /// APNs 토큰이 등록된 후에 자동으로 호출됨
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        print("[AppDelegate] FCM token received: \(token.prefix(20))...")

        // 토큰을 직접 전달하여 서버에 동기화
        FcmTokenSyncer.shared.syncToken(token)
    }
}

// MARK: - Main App

@main
struct ssabreeApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    private let container = RealAppContainer()
//    private let container = FakeAppContainer()

    init() {
        // FcmTokenSyncer 설정
        let notificationService = NotificationServiceImpl()
        FcmTokenSyncer.shared.configure(
            authDataStore: container.authDataStore,
            notificationService: notificationService
        )

        // TabBar Appearance — 앱 테마 적용
        let tabBarAppearance = UITabBarAppearance()
        tabBarAppearance.configureWithOpaqueBackground()
        tabBarAppearance.backgroundColor = UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(red: 0x1F/255, green: 0x1B/255, blue: 0x1D/255, alpha: 1) // DarkColors.surface
                : UIColor(red: 0xFF/255, green: 0xFF/255, blue: 0xFF/255, alpha: 1) // LightColors.surface
        }

        let primaryColor = UIColor(red: 0x64/255, green: 0x95/255, blue: 0xEB/255, alpha: 1)
        let inactiveColor = UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(red: 0xE9/255, green: 0xE3/255, blue: 0xDC/255, alpha: 0.6) // DarkColors.onSurface 60%
                : UIColor(red: 0x1F/255, green: 0x1B/255, blue: 0x17/255, alpha: 0.6) // LightColors.onSurface 60%
        }

        // 선택된 탭 아이템
        let selectedAttributes: [NSAttributedString.Key: Any] = [.foregroundColor: primaryColor]
        let normalAttributes: [NSAttributedString.Key: Any] = [.foregroundColor: inactiveColor]

        for state in [tabBarAppearance.stackedLayoutAppearance, tabBarAppearance.inlineLayoutAppearance, tabBarAppearance.compactInlineLayoutAppearance] {
            state.selected.iconColor = primaryColor
            state.selected.titleTextAttributes = selectedAttributes
            state.normal.iconColor = inactiveColor
            state.normal.titleTextAttributes = normalAttributes
        }

        UITabBar.appearance().standardAppearance = tabBarAppearance
        UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
    }

    var body: some Scene {
        WindowGroup {
            ContentView(container: container)
                .environment(\.themeManager, ThemeManager.shared)
                .environment(\.locale, Locale(identifier: "ko_KR"))
        }
    }
}
