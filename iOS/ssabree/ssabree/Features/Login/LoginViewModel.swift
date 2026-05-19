import Foundation
import Observation

struct LoginUiState {
    var email: String = ""
    var password: String = ""
    var isLoading: Bool = false
    var errorMessage: String?
    var isLoginSuccess: Bool = false
}

@MainActor
@Observable
final class LoginViewModel {
    private(set) var uiState = LoginUiState()

    private let authRepository: AuthRepository

    init(authRepository: AuthRepository) {
        self.authRepository = authRepository
    }

    func onEmailChange(_ newEmail: String) {
        uiState.email = newEmail
        uiState.errorMessage = nil
    }

    func onPasswordChange(_ newPassword: String) {
        uiState.password = newPassword
        uiState.errorMessage = nil
    }

    func login(onLoginSuccess: @escaping () -> Void) {
        let email = uiState.email.trimmingCharacters(in: .whitespacesAndNewlines)
        let password = uiState.password

        guard !email.isEmpty, !password.isEmpty else {
            uiState.errorMessage = "이메일과 비밀번호를 입력해주세요"
            return
        }

        Task {
            uiState.isLoading = true
            uiState.errorMessage = nil

            let result = await authRepository.login(email: email, password: password)

            switch result {
            case .success:
                print("Login succeeded")
                uiState.isLoading = false
                uiState.isLoginSuccess = true

                // 로그인 성공 시 FCM 토큰 서버에 동기화
                FcmTokenSyncer.shared.syncIfAuthenticated()

                onLoginSuccess()

            case .failure(let error):
                print("Login failed: \(error.localizedDescription)")
                uiState.isLoading = false
                uiState.errorMessage = error.localizedDescription
            }
        }
    }

    func clearError() {
        uiState.errorMessage = nil
    }
}
