import SwiftUI
import Observation
import UserNotifications
import Combine

// MARK: - UI State

struct NotificationSettingsUiState {
    var scheduledNotificationEnabled: Bool = true
    var isSaving: Bool = false
    var errorMessage: String? = nil
}

// MARK: - ViewModel

@Observable
@MainActor
final class NotificationSettingsViewModel {
    private let notificationRepository: NotificationRepository
    private let localStore = NotificationSettingLocalStore.shared

    var uiState = NotificationSettingsUiState()

    init(notificationRepository: NotificationRepository) {
        self.notificationRepository = notificationRepository
        uiState.scheduledNotificationEnabled = localStore.isScheduledNotificationEnabled()
    }

    func updateScheduledNotificationSetting(_ enabled: Bool) async {
        uiState.isSaving = true
        uiState.errorMessage = nil

        // iOS에서는 APNS 토큰을 사용, 여기서는 기기 토큰 또는 저장된 FCM 토큰을 가져옴
        // 실제 구현에서는 Firebase Messaging에서 토큰을 가져오거나
        // 앱에서 저장한 device token을 사용해야 함
        let token = getDeviceToken()

        let result: Result<Void, Error>
        if enabled {
            result = await notificationRepository.subscribeScheduledNotification(token: token)
        } else {
            result = await notificationRepository.unsubscribeScheduledNotification(token: token)
        }

        switch result {
        case .success:
            localStore.saveScheduledNotificationEnabled(enabled)
            uiState.scheduledNotificationEnabled = enabled
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isSaving = false
    }

    func clearError() {
        uiState.errorMessage = nil
    }

    private func getDeviceToken() -> String {
        // TODO: 실제로는 Firebase Messaging이나 APNS에서 토큰을 가져와야 함
        // 지금은 UserDefaults에 저장된 토큰을 사용하거나 빈 문자열 반환
        UserDefaults.standard.string(forKey: "fcm_token") ?? ""
    }
}

// MARK: - View

struct NotificationSettingsView: View {
    @State var viewModel: NotificationSettingsViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var isSystemNotificationEnabled = false
    @State private var showErrorToast = false

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            topAppBar

            ScrollView {
                VStack(spacing: 24) {
                    Spacer().frame(height: 4)

                    // 전체 알림 (시스템 설정)
                    systemNotificationSection

                    // 주기적 알림
                    scheduledNotificationSection

                    Spacer().frame(height: 20)
                }
                .padding(.horizontal, 20)
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .onAppear {
            checkSystemNotificationStatus()
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            // 시스템 설정에서 돌아왔을 때 상태 갱신 (Android ON_RESUME과 동일)
            checkSystemNotificationStatus()
        }
        .onChange(of: viewModel.uiState.errorMessage) { _, error in
            if error != nil {
                showErrorToast = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    showErrorToast = false
                    viewModel.clearError()
                }
            }
        }
        .overlay(alignment: .bottom) {
            if showErrorToast, let error = viewModel.uiState.errorMessage {
                Text("설정 변경에 실패했습니다.")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(AppColors.error)
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
                    .padding(.bottom, 20)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showErrorToast)
    }

    // MARK: - Top App Bar

    private var topAppBar: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("알림 설정")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            // Balance
            Image(systemName: "chevron.left")
                .font(.title3)
                .foregroundStyle(.clear)
        }
        .padding()
        .background(AppColors.background)
    }

    // MARK: - System Notification Section

    private var systemNotificationSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("전체 알림")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
                .padding(.leading, 8)

            Text("시스템 설정에서 앱 알림 ON/OFF를 변경해 주세요.")
                .font(.system(size: 13))
                .foregroundStyle(AppColors.onSurfaceVariant)
                .padding(.leading, 8)
                .padding(.bottom, 8)

            Button(action: openAppNotificationSettings) {
                HStack {
                    Text("앱 알림")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundStyle(AppColors.onSurface)

                    Spacer()

                    Toggle("", isOn: $isSystemNotificationEnabled)
                        .labelsHidden()
                        .tint(AppColors.primary)
                        .disabled(true) // 토글 자체로는 변경 불가, 버튼 탭으로만 설정 이동
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 28))
            }
            .buttonStyle(.plain)
        }
    }

    // MARK: - Scheduled Notification Section

    private var scheduledNotificationSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("주기적 알림")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
                .padding(.leading, 8)

            Text("입실/퇴실 알림, 점심 식사 확인 알림을 받습니다.")
                .font(.system(size: 13))
                .foregroundStyle(AppColors.onSurfaceVariant)
                .padding(.leading, 8)
                .padding(.bottom, 8)

            HStack {
                Text("푸시 알림 받기")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(AppColors.onSurface)

                Spacer()

                if viewModel.uiState.isSaving {
                    ProgressView()
                        .frame(width: 24, height: 24)
                } else {
                    Toggle("", isOn: Binding(
                        get: { viewModel.uiState.scheduledNotificationEnabled },
                        set: { newValue in
                            Task {
                                await viewModel.updateScheduledNotificationSetting(newValue)
                            }
                        }
                    ))
                    .labelsHidden()
                    .tint(AppColors.primary)
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 28))
        }
    }

    // MARK: - Helper Methods

    private func checkSystemNotificationStatus() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                isSystemNotificationEnabled = settings.authorizationStatus == .authorized
            }
        }
    }

    private func openAppNotificationSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }
}
