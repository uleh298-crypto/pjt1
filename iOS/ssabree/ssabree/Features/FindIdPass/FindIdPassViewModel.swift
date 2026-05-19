import SwiftUI
import Observation

// MARK: - FindIdPass UI State
// Android FindIdPassUiState와 동일한 구조

struct FindIdPassUiState {
    // 아이디 찾기
    var findIdMattermostId: String = ""
    var findIdGeneration: Int? = nil
    var findIdName: String = ""
    var findIdVerificationCode: String = ""
    var isFindIdGenerationDropdownExpanded: Bool = false
    var isFindIdCodeSent: Bool = false
    var isFindIdLoading: Bool = false
    var findIdError: String? = nil
    var foundEmail: String? = nil

    // 비밀번호 찾기
    var findPassMattermostId: String = ""
    var findPassGeneration: Int? = nil
    var findPassName: String = ""
    var findPassVerificationCode: String = ""
    var isFindPassGenerationDropdownExpanded: Bool = false
    var isFindPassCodeSent: Bool = false
    var isFindPassVerified: Bool = false
    var isFindPassLoading: Bool = false
    var findPassError: String? = nil

    // 비밀번호 재설정
    var newPassword: String = ""
    var newPasswordCheck: String = ""
    var isResetLoading: Bool = false
    var isResetSuccess: Bool = false
}

// MARK: - FindIdPass ViewModel

private let CODE_LENGTH = 6

@MainActor
@Observable
final class FindIdPassViewModel {
    var uiState = FindIdPassUiState()

    private let authRepository: AuthRepository

    init(authRepository: AuthRepository) {
        self.authRepository = authRepository
    }

    // MARK: - Helpers

    private func normalizeMattermostId(_ value: String) -> String {
        var result = value.trimmingCharacters(in: .whitespacesAndNewlines)
        if result.hasPrefix("@") {
            result.removeFirst()
        }
        return result
    }

    // MARK: - 아이디 찾기

    func onFindIdMattermostIdChange(_ value: String) {
        uiState.findIdMattermostId = value
        uiState.findIdError = nil
    }

    func onFindIdNameChange(_ value: String) {
        uiState.findIdName = value
        uiState.findIdError = nil
    }

    func onFindIdGenerationDropdownClick() {
        uiState.isFindIdGenerationDropdownExpanded = true
    }

    func onFindIdGenerationDropdownDismiss() {
        uiState.isFindIdGenerationDropdownExpanded = false
    }

    func onFindIdGenerationSelected(_ value: Int) {
        uiState.findIdGeneration = value
        uiState.isFindIdGenerationDropdownExpanded = false
        uiState.findIdError = nil
    }

    func onFindIdVerificationCodeChange(_ value: String) {
        uiState.findIdVerificationCode = String(value.filter { $0.isNumber }.prefix(CODE_LENGTH))
        uiState.findIdError = nil
    }

    func sendFindIdCode() {
        let mattermostId = normalizeMattermostId(uiState.findIdMattermostId)
        let name = uiState.findIdName
        guard let generation = uiState.findIdGeneration else {
            uiState.findIdError = "이름, Mattermost ID와 기수를 입력해주세요."
            return
        }
        guard !name.isEmpty, !mattermostId.isEmpty else {
            uiState.findIdError = "이름, Mattermost ID와 기수를 입력해주세요."
            return
        }

        Task {
            uiState.isFindIdLoading = true
            uiState.findIdError = nil

            let result = await authRepository.requestSsafyVerification(
                mattermostId: mattermostId,
                generation: generation,
                name: name
            )

            switch result {
            case .success:
                uiState.isFindIdLoading = false
                uiState.isFindIdCodeSent = true
            case .failure(let error):
                uiState.isFindIdLoading = false
                uiState.findIdError = error.localizedDescription
            }
        }
    }

    func verifyFindId() {
        let mattermostId = normalizeMattermostId(uiState.findIdMattermostId)
        let code = uiState.findIdVerificationCode
        guard !mattermostId.isEmpty, code.count == CODE_LENGTH else { return }

        Task {
            uiState.isFindIdLoading = true
            uiState.findIdError = nil

            let verifyResult = await authRepository.confirmSsafyVerification(
                mattermostId: mattermostId,
                code: code
            )

            switch verifyResult {
            case .success:
                // 인증 성공 후 아이디 조회
                let findIdResult = await authRepository.findId(mattermostId: mattermostId)
                switch findIdResult {
                case .success(let email):
                    uiState.isFindIdLoading = false
                    uiState.foundEmail = email
                case .failure(let error):
                    uiState.isFindIdLoading = false
                    uiState.findIdError = error.localizedDescription
                }
            case .failure(let error):
                uiState.isFindIdLoading = false
                uiState.findIdError = error.localizedDescription
            }
        }
    }

    func consumeFoundEmail() {
        uiState.foundEmail = nil
    }

    // MARK: - 비밀번호 찾기

    func onFindPassMattermostIdChange(_ value: String) {
        uiState.findPassMattermostId = value
        uiState.findPassError = nil
    }

    func onFindPassNameChange(_ value: String) {
        uiState.findPassName = value
        uiState.findPassError = nil
    }

    func onFindPassGenerationDropdownClick() {
        uiState.isFindPassGenerationDropdownExpanded = true
    }

    func onFindPassGenerationDropdownDismiss() {
        uiState.isFindPassGenerationDropdownExpanded = false
    }

    func onFindPassGenerationSelected(_ value: Int) {
        uiState.findPassGeneration = value
        uiState.isFindPassGenerationDropdownExpanded = false
        uiState.findPassError = nil
    }

    func onFindPassVerificationCodeChange(_ value: String) {
        uiState.findPassVerificationCode = String(value.filter { $0.isNumber }.prefix(CODE_LENGTH))
        uiState.findPassError = nil
    }

    func sendFindPassCode() {
        let mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        let name = uiState.findPassName
        guard let generation = uiState.findPassGeneration else {
            uiState.findPassError = "이름, Mattermost ID와 기수를 입력해주세요."
            return
        }
        guard !name.isEmpty, !mattermostId.isEmpty else {
            uiState.findPassError = "이름, Mattermost ID와 기수를 입력해주세요."
            return
        }

        Task {
            uiState.isFindPassLoading = true
            uiState.findPassError = nil

            let result = await authRepository.requestSsafyVerification(
                mattermostId: mattermostId,
                generation: generation,
                name: name
            )

            switch result {
            case .success:
                uiState.isFindPassLoading = false
                uiState.isFindPassCodeSent = true
            case .failure(let error):
                uiState.isFindPassLoading = false
                uiState.findPassError = error.localizedDescription
            }
        }
    }

    func verifyFindPass() {
        let mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        let code = uiState.findPassVerificationCode
        guard !mattermostId.isEmpty, code.count == CODE_LENGTH else { return }

        Task {
            uiState.isFindPassLoading = true
            uiState.findPassError = nil

            let result = await authRepository.confirmSsafyVerification(
                mattermostId: mattermostId,
                code: code
            )

            switch result {
            case .success:
                uiState.isFindPassLoading = false
                uiState.isFindPassVerified = true
            case .failure(let error):
                uiState.isFindPassLoading = false
                uiState.findPassError = error.localizedDescription
            }
        }
    }

    func consumeFindPassVerified() {
        uiState.isFindPassVerified = false
    }

    // MARK: - 비밀번호 재설정

    func onNewPasswordChange(_ value: String) {
        uiState.newPassword = value
        uiState.findPassError = nil
    }

    func onNewPasswordCheckChange(_ value: String) {
        uiState.newPasswordCheck = value
        uiState.findPassError = nil
    }

    func resetPassword() {
        let mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        let newPassword = uiState.newPassword
        let newPasswordCheck = uiState.newPasswordCheck

        guard !mattermostId.isEmpty, !newPassword.isEmpty, !newPasswordCheck.isEmpty else { return }
        guard newPassword == newPasswordCheck else {
            uiState.findPassError = "비밀번호가 일치하지 않습니다."
            return
        }

        Task {
            uiState.isResetLoading = true
            uiState.findPassError = nil

            let result = await authRepository.resetPassword(
                mattermostId: mattermostId,
                newPassword: newPassword
            )

            switch result {
            case .success:
                uiState.isResetLoading = false
                uiState.isResetSuccess = true
            case .failure(let error):
                uiState.isResetLoading = false
                uiState.findPassError = error.localizedDescription
            }
        }
    }

    func consumeResetSuccess() {
        uiState.isResetSuccess = false
    }
}
