import SwiftUI

struct FindIdPassView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: FindIdPassViewModel
    @State private var selectedTabIndex = 0
    @State private var findIdStep = 1
    @State private var foundId = ""
    @State private var findPassStep = 1

    let onConfirm: () -> Void
    let onResetConfirm: () -> Void

    private let generations = [14, 15]

    init(viewModel: FindIdPassViewModel, onConfirm: @escaping () -> Void, onResetConfirm: @escaping () -> Void) {
        self._viewModel = State(initialValue: viewModel)
        self.onConfirm = onConfirm
        self.onResetConfirm = onResetConfirm
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top Bar
            FindIdPassTopBar(onBackClick: { dismiss() })

            Spacer().frame(height: 8)

            VStack(spacing: 0) {
                // Tab Row
                FindIdPassTabRow(
                    selectedIndex: $selectedTabIndex,
                    tabs: ["아이디", "비밀번호"]
                )

                Divider()
                    .background(Color.gray.opacity(0.2))

                // Content
                switch selectedTabIndex {
                case 0:
                    findIdContent
                case 1:
                    findPassContent
                default:
                    EmptyView()
                }
            }
            .padding(.horizontal, 24)
        }
        .background(Color(UIColor.systemBackground))
        .navigationBarHidden(true)
        .onChange(of: viewModel.uiState.foundEmail) { _, newValue in
            if let email = newValue {
                foundId = email
                findIdStep = 2
                viewModel.consumeFoundEmail()
            }
        }
        .onChange(of: viewModel.uiState.isFindPassVerified) { _, newValue in
            if newValue {
                findPassStep = 2
                viewModel.consumeFindPassVerified()
            }
        }
        .onChange(of: viewModel.uiState.isResetSuccess) { _, newValue in
            if newValue {
                viewModel.consumeResetSuccess()
                onResetConfirm()
            }
        }
    }

    @ViewBuilder
    private var findIdContent: some View {
        switch findIdStep {
        case 1:
            FindIdStep1View(
                mattermostId: viewModel.uiState.findIdMattermostId,
                onMattermostIdChange: viewModel.onFindIdMattermostIdChange,
                name: viewModel.uiState.findIdName,
                onNameChange: viewModel.onFindIdNameChange,
                generations: generations,
                selectedGeneration: viewModel.uiState.findIdGeneration,
                isGenerationDropdownExpanded: viewModel.uiState.isFindIdGenerationDropdownExpanded,
                onGenerationDropdownClick: viewModel.onFindIdGenerationDropdownClick,
                onGenerationDropdownDismiss: viewModel.onFindIdGenerationDropdownDismiss,
                onGenerationSelected: viewModel.onFindIdGenerationSelected,
                verificationCode: viewModel.uiState.findIdVerificationCode,
                onVerificationCodeChange: viewModel.onFindIdVerificationCodeChange,
                isCodeSent: viewModel.uiState.isFindIdCodeSent,
                isLoading: viewModel.uiState.isFindIdLoading,
                errorMessage: viewModel.uiState.findIdError,
                onSendCode: viewModel.sendFindIdCode,
                onVerify: viewModel.verifyFindId,
                purpose: "아이디 찾기"
            )
        default:
            FindIdStep2View(
                foundId: foundId,
                onConfirm: onConfirm
            )
        }
    }

    @ViewBuilder
    private var findPassContent: some View {
        switch findPassStep {
        case 1:
            FindPassStep1View(
                mattermostId: viewModel.uiState.findPassMattermostId,
                onMattermostIdChange: viewModel.onFindPassMattermostIdChange,
                name: viewModel.uiState.findPassName,
                onNameChange: viewModel.onFindPassNameChange,
                generations: generations,
                selectedGeneration: viewModel.uiState.findPassGeneration,
                isGenerationDropdownExpanded: viewModel.uiState.isFindPassGenerationDropdownExpanded,
                onGenerationDropdownClick: viewModel.onFindPassGenerationDropdownClick,
                onGenerationDropdownDismiss: viewModel.onFindPassGenerationDropdownDismiss,
                onGenerationSelected: viewModel.onFindPassGenerationSelected,
                verificationCode: viewModel.uiState.findPassVerificationCode,
                onVerificationCodeChange: viewModel.onFindPassVerificationCodeChange,
                isCodeSent: viewModel.uiState.isFindPassCodeSent,
                isLoading: viewModel.uiState.isFindPassLoading,
                errorMessage: viewModel.uiState.findPassError,
                onSendCode: viewModel.sendFindPassCode,
                onVerify: viewModel.verifyFindPass,
                purpose: "비밀번호 재설정"
            )
        case 2:
            FindPassStep2View(
                onConfirm: { findPassStep = 3 }
            )
        default:
            FindPassStep3View(
                newPassword: viewModel.uiState.newPassword,
                newPasswordCheck: viewModel.uiState.newPasswordCheck,
                onNewPasswordChange: viewModel.onNewPasswordChange,
                onNewPasswordCheckChange: viewModel.onNewPasswordCheckChange,
                isLoading: viewModel.uiState.isResetLoading,
                errorMessage: viewModel.uiState.findPassError,
                onResetConfirm: viewModel.resetPassword
            )
        }
    }
}

// MARK: - Top Bar

struct FindIdPassTopBar: View {
    let onBackClick: () -> Void

    var body: some View {
        HStack {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(Color(UIColor.label))
            }
            .padding(.leading, 16)

            Spacer()
        }
        .frame(height: 56)
    }
}

// MARK: - Tab Row

struct FindIdPassTabRow: View {
    @Binding var selectedIndex: Int
    let tabs: [String]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.offset) { index, title in
                Button(action: { selectedIndex = index }) {
                    VStack(spacing: 0) {
                        Text(title)
                            .font(.system(size: 14, weight: selectedIndex == index ? .semibold : .regular))
                            .foregroundColor(selectedIndex == index ? .primary : .secondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)

                        Rectangle()
                            .fill(selectedIndex == index ? Color.accentColor : Color.clear)
                            .frame(height: 2)
                    }
                }
            }
        }
    }
}
