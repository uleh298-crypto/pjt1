import SwiftUI

// MARK: - Android Compose → SwiftUI 대응
// TopAppBar → Custom HStack with back button
// Surface + Column + verticalScroll → ScrollView + VStack
// Surface(tonalElevation) → background + clipShape + shadow
// TextField with colors → Custom TextFieldStyle
// Button with ButtonDefaults → Custom button styling
// DropdownMenu → Menu or Picker
// LaunchedEffect → .onChange / .onAppear

struct JoinView: View {
    @State private var viewModel: JoinViewModel
    let onJoinSuccess: () -> Void
    let onNavigationIconClick: () -> Void
    @State private var showJoinSuccessDialog = false

    init(viewModel: JoinViewModel, onJoinSuccess: @escaping () -> Void, onNavigationIconClick: @escaping () -> Void) {
        self._viewModel = State(initialValue: viewModel)
        self.onJoinSuccess = onJoinSuccess
        self.onNavigationIconClick = onNavigationIconClick
    }

    var body: some View {
        // Android: Surface(modifier = Modifier.fillMaxSize(), color = background)
        ZStack {
            AppColors.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Android: TopBar(onBackClick = {...})
                JoinTopBar(
                    onNavigationIconClick: {
                        if !viewModel.goBack() {
                            onNavigationIconClick()
                        }
                    }
                )

                // Android: Spacer(modifier = Modifier.height(8.dp))
                Spacer().frame(height: 8)

                // Android: Column with imePadding, padding(horizontal = 24.dp)
                ScrollView {
                    VStack(spacing: 0) {
                        switch viewModel.uiState.step {
                        case 1:
                            JoinStep1View(viewModel: viewModel)
                        case 2:
                            JoinStep2View(viewModel: viewModel)
                        default:
                            JoinStep3View(viewModel: viewModel, onJoinSuccess: onJoinSuccess)
                        }
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
        .navigationBarHidden(true)
        .onChange(of: viewModel.uiState.isJoinSuccess) { _, isSuccess in
            if isSuccess {
                showJoinSuccessDialog = true
            }
        }
        .alert("회원가입 완료", isPresented: $showJoinSuccessDialog) {
            Button("확인") { onJoinSuccess() }
        } message: {
            Text("회원가입이 완료되었습니다.\n로그인 화면으로 이동합니다.")
        }
    }
}

// MARK: - Top Bar
// Android: TopAppBar with title "회원가입", navigationIcon = ArrowBack

struct JoinTopBar: View {
    let onNavigationIconClick: () -> Void

    var body: some View {
        HStack {
            // Android: IconButton(onClick = onBackClick) { Icon(ArrowBack) }
            Button(action: onNavigationIconClick) {
                Image(systemName: "arrow.backward")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(AppColors.onSurface)
                    .frame(width: 44, height: 44)
            }

            Spacer()

            // Android: Text("회원가입", fontSize = 18.sp, fontWeight = Bold)
            Text("회원가입")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)

            Spacer()

            // Placeholder for symmetry
            Color.clear.frame(width: 44, height: 44)
        }
        .padding(.horizontal, 8)
        .background(AppColors.background)
    }
}

// MARK: - Step Header (공통 로고 + 타이틀)
// Android: Surface(80.dp, RoundedCornerShape(18.dp), tonalElevation=2.dp) { Image(logo) }

struct JoinStepHeader: View {
    var body: some View {
        VStack(spacing: 0) {
            // Logo Surface
            Image("logo")
                .resizable()
                .scaledToFit()
                .padding(8)
                .frame(width: 80, height: 80)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .shadow(color: .black.opacity(0.06), radius: 2, x: 0, y: 1)

            Spacer().frame(height: 16)

            // Android: Text("SSAFY MatterMost 인증", fontSize = 25.sp, fontWeight = Bold)
            Text("SSAFY MatterMost 인증")
                .font(.system(size: 25, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer().frame(height: 4)

            // Android: Text("싸브리타임 이용을 위해...", fontSize = 12.sp, alpha = 0.7f)
            Text("싸브리타임 이용을 위해 개인정보를 입력해주세요.")
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onBackground.opacity(0.7))
                .multilineTextAlignment(.center)
        }
    }
}

// MARK: - Step 1: Personal Info

struct JoinStep1View: View {
    @Bindable var viewModel: JoinViewModel

    var body: some View {
        VStack(spacing: 0) {
            JoinStepHeader()

            Spacer().frame(height: 24)

            // 이름
            JoinLabeledTextField(
                label: "이름",
                placeholder: "이름",
                text: Binding(
                    get: { viewModel.uiState.name },
                    set: { viewModel.onNameChange($0) }
                )
            )

            Spacer().frame(height: 12)

            // 학번
            JoinLabeledTextField(
                label: "학번",
                placeholder: "숫자만 입력",
                text: Binding(
                    get: { viewModel.uiState.studentId },
                    set: { viewModel.onStudentIdChange($0) }
                ),
                keyboardType: .numberPad
            )

            Spacer().frame(height: 12)

            // 기수 드롭다운
            JoinDropdownField(
                label: "기수",
                selectedText: viewModel.uiState.selectedGeneration.map { "\($0)기" } ?? "",
                placeholder: "기수를 선택",
                items: viewModel.uiState.generations,
                itemText: { "\($0)기" },
                onItemSelected: { viewModel.onGenerationSelected($0) }
            )

            Spacer().frame(height: 12)

            // 캠퍼스 드롭다운
            JoinDropdownField(
                label: "캠퍼스",
                selectedText: viewModel.uiState.selectedCampus?.name ?? "",
                placeholder: "캠퍼스를 선택",
                isLoading: viewModel.uiState.isLoadingCampuses,
                items: viewModel.uiState.campuses,
                itemText: { $0.name },
                onItemSelected: { viewModel.onCampusSelected($0) }
            )

            Spacer().frame(height: 12)

            // 반 드롭다운
            // Android: selectedText = selectedClass?.classNo?.toString() ?: selectedClass?.name.orEmpty()
            // Android: itemText = { ban -> ban.classNo?.toString() ?: ban.name }
            JoinDropdownField(
                label: "1학기 반",
                selectedText: {
                    if let ban = viewModel.uiState.selectedClass {
                        if let classNo = ban.classNo {
                            return "\(classNo)반"
                        }
                        return ban.name
                    }
                    return ""
                }(),
                placeholder: {
                    // Android: when { selectedGeneration == null -> "기수를 먼저 선택하세요" ... }
                    if viewModel.uiState.selectedGeneration == nil {
                        return "기수를 먼저 선택하세요"
                    } else if viewModel.uiState.selectedCampus == nil {
                        return "캠퍼스를 먼저 선택하세요"
                    } else {
                        return "반을 선택"
                    }
                }(),
                isLoading: viewModel.uiState.isLoadingClasses,
                isEnabled: viewModel.uiState.selectedCampus != nil && viewModel.uiState.selectedGeneration != nil,
                items: viewModel.uiState.classes,
                itemText: { ban in
                    if let classNo = ban.classNo {
                        return "\(classNo)반"
                    }
                    return ban.name
                },
                onItemSelected: { viewModel.onClassSelected($0) }
            )

            Spacer().frame(height: 24)

            // 다음 버튼
            JoinPrimaryButton(
                title: "다음",
                isEnabled: viewModel.uiState.isStep1Valid,
                isLoading: false,
                action: { viewModel.goToStep(2) }
            )

            Spacer().frame(height: 24)
        }
    }
}

// MARK: - Step 2: Mattermost Verification

struct JoinStep2View: View {
    @Bindable var viewModel: JoinViewModel

    var body: some View {
        VStack(spacing: 0) {
            JoinStepHeader()

            Spacer().frame(height: 32)

            // MatterMost 아이디
            JoinLabeledTextField(
                label: "MatterMost 아이디",
                placeholder: "abcdef",
                text: Binding(
                    get: { viewModel.uiState.mattermostId },
                    set: { viewModel.onMattermostIdChange($0) }
                ),
                isEnabled: !viewModel.uiState.isCodeSent
            )

            Spacer().frame(height: 12)

            // 선택한 기수 표시
            if let generation = viewModel.uiState.selectedGeneration {
                Text("선택한 기수: \(generation)기")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface.opacity(0.7))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Spacer().frame(height: 16)

            // 인증번호 발송 버튼 (코드 발송 전)
            if !viewModel.uiState.isCodeSent {
                JoinPrimaryButton(
                    title: "인증번호 발송",
                    isEnabled: !viewModel.uiState.mattermostId.isEmpty &&
                        viewModel.uiState.selectedGeneration != nil &&
                        !viewModel.uiState.isSendingCode,
                    isLoading: viewModel.uiState.isSendingCode,
                    action: { viewModel.sendVerificationCode() }
                )
            }

            Spacer().frame(height: 24)

            // 인증번호 입력 (코드 발송 후)
            if viewModel.uiState.isCodeSent {
                Text("인증번호 6자리")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onBackground.opacity(0.85))
                    .frame(maxWidth: .infinity, alignment: .leading)

                Spacer().frame(height: 8)

                JoinTextField(
                    placeholder: "인증번호 6자리 입력",
                    text: Binding(
                        get: { viewModel.uiState.verificationCode },
                        set: { viewModel.onVerificationCodeChange($0) }
                    ),
                    keyboardType: .numberPad
                )

                // 에러 메시지
                if let error = viewModel.uiState.error {
                    Spacer().frame(height: 8)
                    Text(error.localizedDescription)
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.error)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                Spacer().frame(height: 32)

                JoinPrimaryButton(
                    title: "인증 완료",
                    isEnabled: viewModel.uiState.verificationCode.count == 6 && !viewModel.uiState.isVerifying,
                    isLoading: viewModel.uiState.isVerifying,
                    action: { viewModel.verifyCode() }
                )

                Spacer().frame(height: 24)
            }

            // 코드 발송 전 에러 메시지
            if !viewModel.uiState.isCodeSent, let error = viewModel.uiState.error {
                Spacer().frame(height: 8)
                Text(error.localizedDescription)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.error)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }
}

// MARK: - Step 3: Account Info

struct JoinStep3View: View {
    @Bindable var viewModel: JoinViewModel
    let onJoinSuccess: () -> Void

    @State private var showPassword = false
    @State private var showPasswordConfirm = false

    var body: some View {
        VStack(spacing: 0) {
            JoinStepHeader()

            Spacer().frame(height: 48)

            // 이메일 + 중복확인
            VStack(alignment: .leading, spacing: 6) {
                Text("이메일")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(AppColors.onSurface)

                HStack(spacing: 8) {
                    JoinTextField(
                        placeholder: "user@example.com",
                        text: Binding(
                            get: { viewModel.uiState.email },
                            set: { viewModel.onEmailChange($0) }
                        ),
                        keyboardType: .emailAddress
                    )

                    // 중복확인 버튼
                    Button(action: { viewModel.checkEmailDuplicate() }) {
                        if viewModel.uiState.isCheckingEmail {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                                .scaleEffect(0.8)
                        } else {
                            Text("중복확인")
                                .font(.system(size: 13))
                        }
                    }
                    .frame(height: 52)
                    .padding(.horizontal, 12)
                    .background(
                        viewModel.uiState.isEmailAvailable == true ?
                        Color(red: 0x4C/255, green: 0xAF/255, blue: 0x50/255).opacity(0.1) :
                        Color.clear
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(AppColors.onSurface.opacity(0.3), lineWidth: 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .disabled(viewModel.uiState.email.isEmpty || viewModel.uiState.isCheckingEmail)
                }
            }

            // 이메일 상태 메시지
            Group {
                switch viewModel.uiState.isEmailAvailable {
                case true:
                    Text("사용 가능한 이메일입니다.")
                        .foregroundStyle(Color(red: 0x4C/255, green: 0xAF/255, blue: 0x50/255))
                case false:
                    Text("이미 사용 중인 이메일입니다.")
                        .foregroundStyle(AppColors.error)
                case nil:
                    Text("이메일 형식으로 입력해 주세요")
                        .foregroundStyle(AppColors.onBackground.opacity(0.7))
                }
            }
            .font(.system(size: 11))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 6)
            .padding(.leading, 2)

            Spacer().frame(height: 24)

            // 비밀번호
            JoinLabeledPasswordField(
                label: "비밀번호",
                placeholder: "비밀번호",
                text: Binding(
                    get: { viewModel.uiState.password },
                    set: { viewModel.onPasswordChange($0) }
                ),
                showPassword: $showPassword
            )

            Spacer().frame(height: 8)

            // 비밀번호 확인
            JoinLabeledPasswordField(
                label: "비밀번호 확인",
                placeholder: "비밀번호 확인",
                text: Binding(
                    get: { viewModel.uiState.passwordConfirm },
                    set: { viewModel.onPasswordConfirmChange($0) }
                ),
                showPassword: $showPasswordConfirm
            )

            // 비밀번호 규칙
            Text("8자 이상, 영문, 숫자, 특수문자(@$!%*#?&) 포함")
                .font(.system(size: 11))
                .foregroundStyle(AppColors.onBackground.opacity(0.7))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.top, 6)
                .padding(.leading, 2)

            // 비밀번호 불일치
            if viewModel.uiState.passwordMismatch {
                Spacer().frame(height: 4)
                Text("비밀번호가 일치하지 않습니다.")
                    .font(.system(size: 11))
                    .foregroundStyle(AppColors.error)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.leading, 2)
            }

            // 에러 메시지
            if let error = viewModel.uiState.error {
                Spacer().frame(height: 8)
                Text(error.localizedDescription)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.error)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Spacer().frame(height: 24)

            // 가입 완료 버튼
            JoinPrimaryButton(
                title: "가입 완료",
                isEnabled: viewModel.uiState.isStep3Valid,
                isLoading: viewModel.uiState.isLoading,
                action: { viewModel.signUp(onSuccess: onJoinSuccess) }
            )

            Spacer().frame(height: 24)
        }
    }
}

// MARK: - Common Components

// 라벨 + 텍스트필드
struct JoinLabeledTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isEnabled: Bool = true

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Android: Text(label, fontSize = 14.sp, fontWeight = SemiBold)
            Text(label)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(AppColors.onSurface)

            JoinTextField(
                placeholder: placeholder,
                text: $text,
                keyboardType: keyboardType,
                isEnabled: isEnabled
            )
        }
    }
}

// 기본 텍스트필드
// Android: TextField with primaryContainer background, RoundedCornerShape(4.dp), height = 52.dp
struct JoinTextField: View {
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isEnabled: Bool = true

    var body: some View {
        TextField(placeholder, text: $text)
            .keyboardType(keyboardType)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .disabled(!isEnabled)
            .padding(.horizontal, 16)
            .frame(height: 52)
            .background(isEnabled ? AppColors.primaryContainer : AppColors.primaryContainer.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 4))
    }
}

// 비밀번호 필드 (보기/숨기기 토글)
struct JoinLabeledPasswordField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    @Binding var showPassword: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(AppColors.onSurface)

            HStack(spacing: 0) {
                Group {
                    if showPassword {
                        TextField(placeholder, text: $text)
                    } else {
                        SecureField(placeholder, text: $text)
                    }
                }
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()

                Button(action: { showPassword.toggle() }) {
                    Image(systemName: showPassword ? "eye" : "eye.slash")
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .frame(height: 52)
            .background(AppColors.primaryContainer)
            .clipShape(RoundedRectangle(cornerRadius: 4))
        }
    }
}

// 드롭다운 필드
// Android: Box with DropdownMenu
struct JoinDropdownField<T: Hashable>: View {
    let label: String
    let selectedText: String
    let placeholder: String
    var isLoading: Bool = false
    var isEnabled: Bool = true
    let items: [T]
    let itemText: (T) -> String
    let onItemSelected: (T) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(AppColors.onSurface)

            Menu {
                ForEach(items, id: \.self) { item in
                    Button(itemText(item)) {
                        onItemSelected(item)
                    }
                }
            } label: {
                HStack {
                    if isLoading {
                        ProgressView()
                            .scaleEffect(0.8)
                    } else {
                        Text(selectedText.isEmpty ? placeholder : selectedText)
                            .foregroundStyle(
                                selectedText.isEmpty ?
                                AppColors.onSurface.opacity(0.6) :
                                AppColors.onSurface
                            )
                    }
                    Spacer()
                    Image(systemName: "chevron.down")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }
                .padding(.horizontal, 16)
                .frame(height: 52)
                .frame(maxWidth: .infinity)
                .background(isEnabled ? AppColors.primaryContainer : AppColors.primaryContainer.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: 4))
            }
            .disabled(!isEnabled || isLoading)
        }
    }
}

// 주요 버튼
// Android: Button with LoginButton color, RoundedCornerShape(5.dp), height = 56.dp
struct JoinPrimaryButton: View {
    let title: String
    let isEnabled: Bool
    let isLoading: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.onPrimary))
                        .scaleEffect(0.8)
                } else {
                    Text(title)
                        .font(.system(size: 16))
                        .foregroundStyle(AppColors.onPrimary)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(isEnabled ? AppColors.loginButton : AppColors.loginButtonDisabled)
            .clipShape(RoundedRectangle(cornerRadius: 5))
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
    }
}
