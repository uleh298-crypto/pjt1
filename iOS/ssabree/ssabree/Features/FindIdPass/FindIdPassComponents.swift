import SwiftUI

// MARK: - Colors

private let loginButtonColor = Color(red: 0.29, green: 0.45, blue: 0.96)

// MARK: - FindIdStep1View (아이디 찾기 인증)

struct FindIdStep1View: View {
    let mattermostId: String
    let onMattermostIdChange: (String) -> Void
    let name: String
    let onNameChange: (String) -> Void
    let generations: [Int]
    let selectedGeneration: Int?
    let isGenerationDropdownExpanded: Bool
    let onGenerationDropdownClick: () -> Void
    let onGenerationDropdownDismiss: () -> Void
    let onGenerationSelected: (Int) -> Void
    let verificationCode: String
    let onVerificationCodeChange: (String) -> Void
    let isCodeSent: Bool
    let isLoading: Bool
    let errorMessage: String?
    let onSendCode: () -> Void
    let onVerify: () -> Void
    let purpose: String

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 40)

                // Logo
                FindLogoView()

                Spacer().frame(height: 16)

                // Title
                Text("SSAFY MatterMost 인증")
                    .font(.system(size: 25, weight: .bold))
                    .foregroundColor(Color(UIColor.label))

                Spacer().frame(height: 4)

                // Subtitle
                Text("\(purpose)를 위해 인증을 완료해 주세요.")
                    .font(.system(size: 12))
                    .foregroundColor(Color(UIColor.label).opacity(0.7))

                Spacer().frame(height: 32)

                // Name field
                VStack(alignment: .leading, spacing: 6) {
                    Text("이름")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color(UIColor.label))

                    FindTextField(
                        value: name,
                        onValueChange: onNameChange,
                        placeholder: "이름"
                    )
                }

                Spacer().frame(height: 12)

                // MattermostId + Generation
                VStack(alignment: .leading, spacing: 6) {
                    Text("Mattermost 아이디")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color(UIColor.label))

                    HStack(spacing: 8) {
                        FindTextField(
                            value: mattermostId,
                            onValueChange: onMattermostIdChange,
                            placeholder: "abcdef"
                        )

                        FindGenerationDropdown(
                            generations: generations,
                            selectedGeneration: selectedGeneration,
                            expanded: isGenerationDropdownExpanded,
                            onDropdownClick: onGenerationDropdownClick,
                            onDismiss: onGenerationDropdownDismiss,
                            onGenerationSelected: onGenerationSelected
                        )
                    }
                }

                Spacer().frame(height: 16)

                // Send Code Button
                if !isCodeSent {
                    FindPrimaryButton(
                        text: isLoading ? "전송 중..." : "인증번호 발송",
                        enabled: !mattermostId.isEmpty && !name.isEmpty && selectedGeneration != nil && !isLoading,
                        onClick: onSendCode
                    )
                }

                Spacer().frame(height: 24)

                // Verification code section
                if isCodeSent {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("인증번호 6자리")
                            .font(.system(size: 13))
                            .foregroundColor(Color(UIColor.label).opacity(0.85))
                            .frame(maxWidth: .infinity, alignment: .leading)

                        FindTextField(
                            value: verificationCode,
                            onValueChange: onVerificationCodeChange,
                            placeholder: "인증번호 6자리 입력",
                            keyboardType: .numberPad
                        )
                        .frame(height: 56)

                        if let error = errorMessage {
                            Spacer().frame(height: 8)
                            Text(error)
                                .font(.system(size: 12))
                                .foregroundColor(.red)
                        }

                        Spacer().frame(height: 32)

                        FindPrimaryButton(
                            text: isLoading ? "조회 중..." : "인증 완료",
                            enabled: verificationCode.count == 6 && !isLoading,
                            onClick: onVerify
                        )

                        Spacer().frame(height: 24)
                    }
                }

                // Error message (when code not sent)
                if !isCodeSent, let error = errorMessage {
                    Spacer().frame(height: 8)
                    Text(error)
                        .font(.system(size: 12))
                        .foregroundColor(.red)
                }
            }
        }
    }
}

// MARK: - FindIdStep2View (아이디 찾기 결과)

struct FindIdStep2View: View {
    let foundId: String
    let onConfirm: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 80)

            FindLogoView()

            Spacer().frame(height: 16)

            Text("싸용자님의 아이디는\n'\(foundId)'\n입니다.")
                .font(.system(size: 14, weight: .semibold))
                .multilineTextAlignment(.center)
                .foregroundColor(Color(UIColor.label))

            Spacer().frame(height: 40)

            Button(action: onConfirm) {
                Text("확인")
                    .font(.system(size: 14))
                    .foregroundColor(.white)
                    .frame(width: 140, height: 48)
                    .background(loginButtonColor)
                    .cornerRadius(16)
            }

            Spacer()
        }
    }
}

// MARK: - FindPassStep1View (비밀번호 찾기 인증)

struct FindPassStep1View: View {
    let mattermostId: String
    let onMattermostIdChange: (String) -> Void
    let name: String
    let onNameChange: (String) -> Void
    let generations: [Int]
    let selectedGeneration: Int?
    let isGenerationDropdownExpanded: Bool
    let onGenerationDropdownClick: () -> Void
    let onGenerationDropdownDismiss: () -> Void
    let onGenerationSelected: (Int) -> Void
    let verificationCode: String
    let onVerificationCodeChange: (String) -> Void
    let isCodeSent: Bool
    let isLoading: Bool
    let errorMessage: String?
    let onSendCode: () -> Void
    let onVerify: () -> Void
    let purpose: String

    var body: some View {
        // FindIdStep1View와 동일한 구조 재사용
        FindIdStep1View(
            mattermostId: mattermostId,
            onMattermostIdChange: onMattermostIdChange,
            name: name,
            onNameChange: onNameChange,
            generations: generations,
            selectedGeneration: selectedGeneration,
            isGenerationDropdownExpanded: isGenerationDropdownExpanded,
            onGenerationDropdownClick: onGenerationDropdownClick,
            onGenerationDropdownDismiss: onGenerationDropdownDismiss,
            onGenerationSelected: onGenerationSelected,
            verificationCode: verificationCode,
            onVerificationCodeChange: onVerificationCodeChange,
            isCodeSent: isCodeSent,
            isLoading: isLoading,
            errorMessage: errorMessage,
            onSendCode: onSendCode,
            onVerify: onVerify,
            purpose: purpose
        )
    }
}

// MARK: - FindPassStep2View (비밀번호 재설정 안내)

struct FindPassStep2View: View {
    let onConfirm: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 80)

            FindLogoView()

            Spacer().frame(height: 16)

            Text("싸용자님의 비밀번호를\n초기화 합니다.")
                .font(.system(size: 14, weight: .semibold))
                .multilineTextAlignment(.center)
                .foregroundColor(Color(UIColor.label))

            Spacer().frame(height: 40)

            Button(action: onConfirm) {
                Text("확인")
                    .font(.system(size: 14))
                    .foregroundColor(.white)
                    .frame(width: 140, height: 48)
                    .background(loginButtonColor)
                    .cornerRadius(16)
            }

            Spacer()
        }
    }
}

// MARK: - FindPassStep3View (비밀번호 재설정)

struct FindPassStep3View: View {
    let newPassword: String
    let newPasswordCheck: String
    let onNewPasswordChange: (String) -> Void
    let onNewPasswordCheckChange: (String) -> Void
    let isLoading: Bool
    let errorMessage: String?
    let onResetConfirm: () -> Void

    private var isResetEnabled: Bool {
        !newPassword.isEmpty && !newPasswordCheck.isEmpty && !isLoading
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 40)

                FindLogoView()

                Spacer().frame(height: 16)

                Text("비밀번호를 재설정해주세요.")
                    .font(.system(size: 12))
                    .foregroundColor(Color(UIColor.label).opacity(0.7))

                Spacer().frame(height: 24)

                // Password field
                FindLabeledField(
                    label: "비밀번호",
                    value: newPassword,
                    onValueChange: onNewPasswordChange,
                    placeholder: "비밀번호",
                    isSecure: true
                )

                Spacer().frame(height: 10)

                // Password confirm field
                FindLabeledField(
                    label: "비밀번호 확인",
                    value: newPasswordCheck,
                    onValueChange: onNewPasswordCheckChange,
                    placeholder: "비밀번호 확인",
                    isSecure: true
                )

                Text("6~20자의 영문, 숫자, 특수문자 중 2개 이상 조합")
                    .font(.system(size: 11))
                    .foregroundColor(Color(UIColor.label).opacity(0.7))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 6)
                    .padding(.leading, 2)

                Spacer().frame(height: 12)

                if let error = errorMessage {
                    Text(error)
                        .font(.system(size: 12))
                        .foregroundColor(.red)
                }

                Spacer().frame(height: 20)

                Button(action: onResetConfirm) {
                    Text(isLoading ? "재설정 중..." : "재설정 완료")
                        .font(.system(size: 16))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(isResetEnabled ? loginButtonColor : loginButtonColor.opacity(0.35))
                        .cornerRadius(20)
                }
                .disabled(!isResetEnabled)

                Spacer().frame(height: 24)
            }
        }
    }
}

// MARK: - Common Components

struct FindLogoView: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 18)
                .fill(Color(UIColor.secondarySystemBackground))
                .frame(width: 80, height: 80)
                .shadow(color: .black.opacity(0.1), radius: 2, y: 1)

            Image("logo")
                .resizable()
                .scaledToFit()
                .frame(width: 64, height: 64)
        }
    }
}

struct FindTextField: View {
    let value: String
    let onValueChange: (String) -> Void
    let placeholder: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        TextField(placeholder, text: Binding(
            get: { value },
            set: { onValueChange($0) }
        ))
        .keyboardType(keyboardType)
        .padding(.horizontal, 12)
        .frame(height: 52)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(4)
    }
}

struct FindLabeledField: View {
    let label: String
    let value: String
    let onValueChange: (String) -> Void
    let placeholder: String
    var isSecure: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(Color(UIColor.label))

            if isSecure {
                SecureField(placeholder, text: Binding(
                    get: { value },
                    set: { onValueChange($0) }
                ))
                .padding(.horizontal, 12)
                .frame(height: 52)
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(4)
            } else {
                TextField(placeholder, text: Binding(
                    get: { value },
                    set: { onValueChange($0) }
                ))
                .padding(.horizontal, 12)
                .frame(height: 52)
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(4)
            }
        }
    }
}

struct FindGenerationDropdown: View {
    let generations: [Int]
    let selectedGeneration: Int?
    let expanded: Bool
    let onDropdownClick: () -> Void
    let onDismiss: () -> Void
    let onGenerationSelected: (Int) -> Void

    var body: some View {
        Menu {
            ForEach(generations, id: \.self) { generation in
                Button("\(generation)기") {
                    onGenerationSelected(generation)
                }
            }
        } label: {
            HStack {
                Text(selectedGeneration.map { "\($0)기" } ?? "기수")
                    .foregroundColor(selectedGeneration == nil ? Color(UIColor.placeholderText) : Color(UIColor.label))

                Spacer()

                Image(systemName: "chevron.down")
                    .foregroundColor(Color(UIColor.placeholderText))
            }
            .padding(.horizontal, 12)
            .frame(width: 100, height: 52)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(4)
        }
    }
}

struct FindPrimaryButton: View {
    let text: String
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Text(text)
                .font(.system(size: 16))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(enabled ? loginButtonColor : loginButtonColor.opacity(0.35))
                .cornerRadius(5)
        }
        .disabled(!enabled)
    }
}
