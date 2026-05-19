import SwiftUI

struct SettingView: View {
    let authRepository: AuthRepository
    let onLogout: () -> Void

    @Environment(\.themeManager) private var themeManager
    @State private var showThemeSheet = false

    // 회원 탈퇴 상태
    @State private var showWithdrawAlert = false
    @State private var showWithdrawConfirmAlert = false
    @State private var showWithdrawError = false
    @State private var withdrawErrorMessage = ""
    @State private var isWithdrawing = false

    private var appVersion: String {
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        return "\(version) V"
    }

    var body: some View {
        VStack(spacing: 0) {
            SimpleHeader(title: "설정")

            ScrollView {
                VStack(spacing: 24) {
                    // 앱 설정
                    SettingSection(title: "앱 설정") {
                        // 다크 모드
                        SettingRow(
                            title: "다크 모드",
                            value: themeManager.themeMode.displayName
                        ) {
                            showThemeSheet = true
                        }

                        Divider()

                        // 알림 설정 → 알림 설정 화면으로 이동
                        SettingNavigationRow(title: "알림 설정", route: .notificationSettings)
                    }

                    // 커뮤니티
                    SettingSection(title: "커뮤니티") {
                        SettingNavigationRow(title: "이용 규칙", route: .communityRules)
                    }

                    // 이용안내
                    SettingSection(title: "이용안내") {
                        SettingRow(title: "앱 버전", value: appVersion) {}

                        Divider()

                        SettingNavigationRow(title: "문의사항", route: .inquiry)

                        Divider()

                        SettingNavigationRow(title: "서비스 이용 약관", route: .termsOfService)
                    }

                    // 계정
                    SettingSection(title: "계정") {
                        SettingRow(title: "회원 탈퇴", isDestructive: true) {
                            showWithdrawAlert = true
                        }
                        .disabled(isWithdrawing)
                    }
                }
                .padding(.vertical)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .sheet(isPresented: $showThemeSheet) {
            ThemeSelectionSheet(
                currentMode: themeManager.themeMode,
                onSelect: { mode in
                    themeManager.setTheme(mode)
                    showThemeSheet = false
                }
            )
            .presentationDetents([.height(280)])
            .presentationDragIndicator(.visible)
        }
        .alert("회원 탈퇴", isPresented: $showWithdrawAlert) {
            Button("취소", role: .cancel) {}
            Button("탈퇴", role: .destructive) {
                showWithdrawConfirmAlert = true
            }
        } message: {
            Text("탈퇴 시 모든 데이터가 영구 삭제되며, 복구할 수 없습니다.\n정말 탈퇴하시겠습니까?")
        }
        .alert("정말로 탈퇴하시겠습니까?", isPresented: $showWithdrawConfirmAlert) {
            Button("취소", role: .cancel) {}
            Button("탈퇴", role: .destructive) {
                performWithdraw()
            }
        } message: {
            Text("이 작업은 되돌릴 수 없습니다.")
        }
        .alert("회원 탈퇴 실패", isPresented: $showWithdrawError) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(withdrawErrorMessage)
        }
    }

    private func performWithdraw() {
        isWithdrawing = true
        Task {
            let result = await authRepository.withdraw()
            await MainActor.run {
                isWithdrawing = false
                switch result {
                case .success:
                    onLogout()
                case .failure(let error):
                    if let apiError = error as? APIError,
                       case .serverMessage = apiError {
                        withdrawErrorMessage = "팀 또는 스터디에 소속되어 있습니다.\n먼저 모든 그룹을 탈퇴한 후 다시 시도해주세요."
                    } else {
                        withdrawErrorMessage = "회원 탈퇴에 실패했습니다. 다시 시도해주세요."
                    }
                    showWithdrawError = true
                }
            }
        }
    }
}

// MARK: - Setting Section

private struct SettingSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)
                .padding(.bottom, 8)
                .padding(.horizontal)

            VStack(spacing: 0) {
                content
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 28))
            .padding(.horizontal)
        }
    }
}

// MARK: - Setting Row

private struct SettingRow: View {
    let title: String
    var value: String? = nil
    var isDestructive: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(isDestructive ? AppColors.error : AppColors.onSurface)
                Spacer()
                if let value = value {
                    Text(value)
                        .font(.subheadline)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }
            }
            .padding(.vertical, 16)
        }
    }
}

// MARK: - Setting Navigation Row

private struct SettingNavigationRow: View {
    let title: String
    let route: AppRoute

    var body: some View {
        NavigationLink(value: route) {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(AppColors.onSurface)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.4))
            }
            .padding(.vertical, 16)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Theme Selection Sheet

private struct ThemeSelectionSheet: View {
    let currentMode: ThemeMode
    let onSelect: (ThemeMode) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Header
            Text("다크 모드")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)
                .padding(.top, 20)
                .padding(.bottom, 16)

            Divider()

            // Options
            VStack(spacing: 0) {
                ForEach(ThemeMode.allCases, id: \.self) { mode in
                    Button(action: { onSelect(mode) }) {
                        HStack {
                            Text(mode.displayName)
                                .font(.body)
                                .foregroundStyle(AppColors.onSurface)
                            Spacer()
                            if mode == currentMode {
                                Image(systemName: "checkmark")
                                    .foregroundStyle(AppColors.primary)
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.vertical, 16)
                    }

                    if mode != ThemeMode.allCases.last {
                        Divider()
                            .padding(.horizontal, 24)
                    }
                }
            }

            Spacer()
        }
        .background(AppColors.surface)
    }
}
