import Foundation
import FirebaseMessaging

/// FCM 토큰을 서버와 동기화하는 유틸리티
/// Android의 FcmTokenSyncer와 동일한 역할
/// 로그인된 세션이 있을 때만 토큰을 동기화
final class FcmTokenSyncer {
    static let shared = FcmTokenSyncer()

    private var authDataStore: AuthDataStore?
    private var notificationService: NotificationService?

    private init() {}

    /// AppDelegate에서 초기화 시 호출
    func configure(authDataStore: AuthDataStore, notificationService: NotificationService) {
        self.authDataStore = authDataStore
        self.notificationService = notificationService
    }

    /// 로그인된 세션이 있을 때만 FCM 토큰을 서버와 동기화
    /// - Note: APNs 토큰이 등록된 후에 호출해야 함 (시뮬레이터에서는 동작하지 않음)
    func syncIfAuthenticated() {
        guard let authDataStore = authDataStore,
              authDataStore.getAccessToken() != nil else {
            print("[FcmTokenSyncer] Not authenticated, skipping token sync")
            return
        }

        // APNs 토큰이 있는지 먼저 확인
        guard Messaging.messaging().apnsToken != nil else {
            print("[FcmTokenSyncer] APNs token not available yet, will sync when token is received")
            return
        }

        Messaging.messaging().token { [weak self] token, error in
            if let error = error {
                print("[FcmTokenSyncer] FCM token fetch failed: \(error.localizedDescription)")
                return
            }

            guard let token = token else {
                print("[FcmTokenSyncer] FCM token is nil")
                return
            }

            self?.syncToken(token)
        }
    }

    /// FCM 토큰을 직접 전달받아 서버에 동기화
    /// MessagingDelegate의 didReceiveRegistrationToken에서 호출
    func syncToken(_ token: String) {
        guard !token.isEmpty else { return }

        guard let authDataStore = authDataStore,
              authDataStore.getAccessToken() != nil else {
            print("[FcmTokenSyncer] Not authenticated, saving token locally only")
            // 로그인 안 되어 있어도 토큰은 저장 (로그인 후 사용)
            UserDefaults.standard.set(token, forKey: "fcm_token")
            return
        }

        // UserDefaults에 토큰 저장 (알림 설정에서 사용)
        UserDefaults.standard.set(token, forKey: "fcm_token")

        Task {
            do {
                // 1. 토큰 등록
                try await notificationService?.registerToken(token: token)
                print("[FcmTokenSyncer] FCM token registered successfully")

                // 2. 로그인 시 항상 구독 활성화 (Android와 동일)
                try await notificationService?.subscribe(token: token)
                NotificationSettingLocalStore.shared.saveScheduledNotificationEnabled(true)
                print("[FcmTokenSyncer] FCM subscription activated")
            } catch {
                print("[FcmTokenSyncer] FCM token sync failed: \(error.localizedDescription)")
            }
        }
    }
}
