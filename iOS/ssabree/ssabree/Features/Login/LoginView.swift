import SwiftUI

// MARK: - Android Compose → SwiftUI 대응표
// ┌─────────────────────────────────────┬─────────────────────────────────────┐
// │ Android Compose                     │ SwiftUI                             │
// ├─────────────────────────────────────┼─────────────────────────────────────┤
// │ Surface                             │ background + color                  │
// │ Box                                 │ ZStack                              │
// │ Column                              │ VStack                              │
// │ Row                                 │ HStack                              │
// │ Spacer(Modifier.height(x.dp))       │ Spacer().frame(height: x)           │
// │ Modifier.fillMaxSize()              │ .frame(maxWidth/Height: .infinity)  │
// │ Modifier.fillMaxWidth()             │ .frame(maxWidth: .infinity)         │
// │ Modifier.padding(horizontal=x.dp)   │ .padding(.horizontal, x)            │
// │ Modifier.size(x.dp)                 │ .frame(width: x, height: x)         │
// │ verticalScroll(rememberScrollState) │ ScrollView                          │
// │ RoundedCornerShape(x.dp)            │ RoundedRectangle(cornerRadius: x)   │
// │ Surface(shadowElevation=x.dp)       │ .shadow(radius: x)                  │
// │ TextField                           │ TextField + custom styling          │
// │ Button                              │ Button                              │
// │ CircularProgressIndicator           │ ProgressView                        │
// │ Image(painterResource)              │ Image("name")                       │
// │ Text                                │ Text                                │
// │ IconButton                          │ Button with Image                   │
// │ Icon(Icons.Filled.Visibility)       │ Image(systemName: "eye")            │
// │ PasswordVisualTransformation        │ SecureField or isSecure toggle      │
// │ LaunchedEffect                      │ .onChange / .onAppear / .task       │
// │ remember { mutableStateOf }         │ @State                              │
// │ MaterialTheme.colorScheme.x         │ AppColors.x                         │
// │ Modifier.imePadding()               │ 자동 (iOS keyboard avoidance)         │
// │ Modifier.navigationBarsPadding()    │ .ignoresSafeArea(edges:) 반대         │
// └─────────────────────────────────────┴─────────────────────────────────────┘

struct LoginView: View {
    // Android: val viewModel: LoginViewModel = simpleViewModel { ... }
    @State private var viewModel: LoginViewModel

    // Android: onLoginSuccess, onJoinClick, onFindIdPassClick 콜백
    let onJoinTap: () -> Void
    let onLoginSuccess: () -> Void
    let onFindIdPassTap: () -> Void

    // Android: var showPassword by remember { mutableStateOf(false) }
    @State private var showPassword = false

    init(
        viewModel: LoginViewModel,
        onJoinTap: @escaping () -> Void,
        onLoginSuccess: @escaping () -> Void,
        onFindIdPassTap: @escaping () -> Void
    ) {
        self._viewModel = State(initialValue: viewModel)
        self.onJoinTap = onJoinTap
        self.onLoginSuccess = onLoginSuccess
        self.onFindIdPassTap = onFindIdPassTap
    }

    var body: some View {
        // Android: Surface(modifier = Modifier.fillMaxSize().navigationBarsPadding(), color = background)
        ZStack {
            AppColors.background
                .ignoresSafeArea()

            // Android: Box(modifier = Modifier.fillMaxSize().imePadding().padding(horizontal=24.dp, vertical=16.dp))
            ScrollView {
                // Android: Column(modifier = Modifier.fillMaxSize().verticalScroll(...), horizontalAlignment = CenterHorizontally)
                VStack(spacing: 0) {

                    // Android: Spacer(modifier = Modifier.height(64.dp))
                    Spacer().frame(height: 64)

                    // Android: Image(painter = painterResource(R.drawable.login_logo), modifier = Modifier.size(104.dp))
                    Image("login_logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 104, height: 104)
                        .padding(.trailing, 16)

                    // Android: Spacer(modifier = Modifier.height(24.dp))
                    Spacer().frame(height: 24)

                    // Android: Image(painter = painterResource(R.drawable.login_comment), modifier = Modifier.size(24.dp))
                    Image("login_comment")
                        .resizable()
                        .scaledToFit()
                        .frame(height: 24)
                        .padding(.trailing, 16)

                    // Android: Spacer(modifier = Modifier.height(56.dp))
                    Spacer().frame(height: 56)

                    // Android: Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 2.dp)
                    loginFormCard

                    // Android: Spacer(modifier = Modifier.height(32.dp))
                    Spacer().frame(height: 32)

                    // Android: Button(onClick = { viewModel.login() }, modifier = Modifier.fillMaxWidth().height(64.dp))
                    loginButton

                    // Android: uiState.errorMessage?.let { ... }
                    if let errorMessage = viewModel.uiState.errorMessage {
                        Spacer().frame(height: 8)
                        Text(errorMessage)
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.error)
                    }

                    // Android: Spacer(modifier = Modifier.height(16.dp))
                    Spacer().frame(height: 16)

                    // Android: Text("이메일/비밀번호 찾기", modifier = Modifier.noRippleClickable { onFindIdPassClick() })
                    Button(action: onFindIdPassTap) {
                        Text("이메일/비밀번호 찾기")
                            .font(.system(size: 13))
                            .foregroundStyle(AppColors.primary)
                    }
                    .buttonStyle(.plain)

                    // Android: Spacer(modifier = Modifier.height(16.dp))
                    Spacer().frame(height: 16)

                    // Android: Row(modifier = Modifier.padding(bottom = 16.dp))
                    HStack(spacing: 0) {
                        Text("싸브리타임이 처음이신가요? ")
                            .font(.system(size: 13))
                            .foregroundStyle(AppColors.onSurface)

                        // Android: Text("회원가입", modifier = Modifier.noRippleClickable { onJoinClick() })
                        Button(action: onJoinTap) {
                            Text("회원가입")
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.primary)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.bottom, 16)
                }
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
            }
        }
        // Android: LaunchedEffect(uiState.isLoginSuccess) { if (isLoginSuccess) onLoginSuccess() }
        .onChange(of: viewModel.uiState.isLoginSuccess) { _, isSuccess in
            if isSuccess {
                onLoginSuccess()
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: - Login Form Card
    // Android: Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 2.dp) { Column(...) }
    private var loginFormCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 이메일 라벨
            // Android: Text("이메일", style = labelMedium, color = onSurface)
            Text("이메일")
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(AppColors.onSurface)

            // 이메일 입력 필드
            // Android: TextField(value, onValueChange, placeholder, shape = RoundedCornerShape(12.dp), colors = ...)
            TextField("이메일", text: Binding(
                get: { viewModel.uiState.email },
                set: { viewModel.onEmailChange($0) }
            ))
            .textFieldStyle(LoginTextFieldStyle())
            .textContentType(.username)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .keyboardType(.emailAddress)
            .submitLabel(.next)

            // 비밀번호 라벨
            // Android: Text("비밀번호", style = labelMedium, color = onSurface)
            Text("비밀번호")
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(AppColors.onSurface)

            // 비밀번호 입력 필드
            // Android: TextField with visualTransformation, trailingIcon for visibility toggle
            HStack(spacing: 0) {
                Group {
                    if showPassword {
                        TextField("비밀번호", text: Binding(
                            get: { viewModel.uiState.password },
                            set: { viewModel.onPasswordChange($0) }
                        ))
                    } else {
                        SecureField("비밀번호", text: Binding(
                            get: { viewModel.uiState.password },
                            set: { viewModel.onPasswordChange($0) }
                        ))
                    }
                }
                .textContentType(.password)
                .submitLabel(.done)

                // Android: IconButton(onClick = { showPassword = !showPassword }) { Icon(Visibility/VisibilityOff) }
                Button(action: { showPassword.toggle() }) {
                    Image(systemName: showPassword ? "eye" : "eye.slash")
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .frame(width: 24, height: 24)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(AppColors.primaryContainer)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .padding(18)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        // Android: shadowElevation = 2.dp
        .shadow(color: .black.opacity(0.08), radius: 4, x: 0, y: 2)
    }

    // MARK: - Login Button
    // Android: Button(enabled = !isLoading && email.isNotBlank() && password.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = LoginButton))
    private var loginButton: some View {
        let isEnabled = !viewModel.uiState.isLoading &&
            !viewModel.uiState.email.trimmingCharacters(in: .whitespaces).isEmpty &&
            !viewModel.uiState.password.isEmpty

        return Button(action: {
            viewModel.login(onLoginSuccess: onLoginSuccess)
        }) {
            ZStack {
                if viewModel.uiState.isLoading {
                    // Android: CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.onPrimary))
                        .scaleEffect(0.8)
                } else {
                    // Android: Text("로그인", fontSize = 16.sp, color = onPrimary)
                    Text("로그인")
                        .font(.system(size: 16))
                        .foregroundStyle(AppColors.onPrimary)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 64)
            // Android: shape = RoundedCornerShape(5.dp)
            .background(isEnabled ? AppColors.loginButton : AppColors.loginButtonDisabled)
            .clipShape(RoundedRectangle(cornerRadius: 5))
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
    }
}

// MARK: - Custom TextField Style
// Android TextField의 colors 설정을 SwiftUI TextFieldStyle로 구현
struct LoginTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            // Android: focusedContainerColor/unfocusedContainerColor = primaryContainer
            .background(AppColors.primaryContainer)
            // Android: shape = RoundedCornerShape(12.dp)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            // Android: focusedIndicatorColor/unfocusedIndicatorColor = Transparent
            // SwiftUI에서는 기본적으로 indicator가 없음
    }
}

// MARK: - Preview
#Preview {
    LoginView(
        viewModel: LoginViewModel(authRepository: FakeAuthRepository()),
        onJoinTap: {},
        onLoginSuccess: {},
        onFindIdPassTap: {}
    )
}
