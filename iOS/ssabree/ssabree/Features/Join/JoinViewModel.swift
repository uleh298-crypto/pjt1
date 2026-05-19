import SwiftUI
import Observation

// MARK: - Debug Log Helper
#if DEBUG
func joinLog(_ message: String) {
    print("[JoinVM] \(message)")
}
#else
func joinLog(_ message: String) {}
#endif

// MARK: - Join Error

enum JoinError: LocalizedError {
    case validation(String)
    case server(String)
    case network
    case unknown

    var errorDescription: String? {
        switch self {
        case .validation(let msg): return msg
        case .server(let msg): return msg
        case .network: return "네트워크 연결을 확인해주세요"
        case .unknown: return "알 수 없는 오류가 발생했습니다"
        }
    }
}

// MARK: - Join UI State
// Android JoinUiState와 동일한 구조

struct JoinUiState {
    // Step 1: 개인 정보
    var name: String = ""
    var studentId: String = ""

    // 기수 선택
    var generations: [Int] = [14, 15]
    var selectedGeneration: Int? = nil
    var isGenerationDropdownExpanded: Bool = false

    // 캠퍼스 선택
    var campuses: [Campus] = []
    var selectedCampus: Campus? = nil
    var isCampusDropdownExpanded: Bool = false
    var isLoadingCampuses: Bool = false

    // 반 선택 - Android와 동일하게 Ban 객체 사용
    var campusClasses: [Ban] = []  // 캠퍼스의 전체 반 목록
    var classes: [Ban] = []         // 기수로 필터링된 반 목록
    var selectedClass: Ban? = nil
    var isClassDropdownExpanded: Bool = false
    var isLoadingClasses: Bool = false

    // Step 2: Mattermost 인증
    var mattermostId: String = ""
    var verificationCode: String = ""
    var isCodeSent: Bool = false
    var isVerified: Bool = false
    var isSendingCode: Bool = false
    var isVerifying: Bool = false

    // Step 3: 계정 정보
    var email: String = ""
    var isCheckingEmail: Bool = false
    var isEmailAvailable: Bool? = nil
    var password: String = ""
    var passwordConfirm: String = ""

    // 공통
    var step: Int = 1
    var isLoading: Bool = false
    var error: JoinError? = nil
    var isJoinSuccess: Bool = false

    // Computed properties - Android와 동일
    var isStep1Valid: Bool {
        !name.isEmpty &&
        !studentId.isEmpty &&
        selectedGeneration != nil &&
        selectedCampus != nil &&
        selectedClass != nil
    }

    var isStep2Valid: Bool {
        verificationCode.count == 6
    }

    var isStep3Valid: Bool {
        !email.isEmpty &&
        !password.isEmpty &&
        !passwordConfirm.isEmpty &&
        password == passwordConfirm &&
        !isLoading
    }

    var passwordMismatch: Bool {
        !passwordConfirm.isEmpty && password != passwordConfirm
    }
}

// MARK: - Join ViewModel

@MainActor
@Observable
final class JoinViewModel {
    var uiState = JoinUiState()

    private let authRepository: AuthRepository
    private let campusRepository: CampusRepository

    init(authRepository: AuthRepository, campusRepository: CampusRepository) {
        self.authRepository = authRepository
        self.campusRepository = campusRepository
        loadCampuses()
    }

    // MARK: - Load Data

    private func loadCampuses() {
        Task {
            joinLog("loadCampuses() called")
            uiState.isLoadingCampuses = true

            let result = await campusRepository.getCampuses()

            switch result {
            case .success(let campusList):
                joinLog("loadCampuses SUCCESS: \(campusList.count) campuses - \(campusList.map { $0.name })")
                uiState.isLoadingCampuses = false
                uiState.campuses = campusList
            case .failure(let error):
                joinLog("loadCampuses FAILED: \(error)")
                uiState.isLoadingCampuses = false
                uiState.error = toJoinError(error)
            }
        }
    }

    private func loadClasses(campusId: Int) {
        Task {
            joinLog("loadClasses(campusId: \(campusId)) called")
            uiState.isLoadingClasses = true
            uiState.classes = []
            uiState.selectedClass = nil

            let result = await campusRepository.getClasses(campusId: campusId)

            switch result {
            case .success(let classList):
                joinLog("loadClasses SUCCESS: \(classList.count) classes")
                joinLog("classList generations: \(Set(classList.compactMap { $0.generation }))")
                joinLog("classList campusIds: \(Set(classList.map { $0.campusId }))")
                uiState.isLoadingClasses = false
                uiState.campusClasses = classList
                // 기수와 캠퍼스로 필터링
                let filtered = filterClasses(source: classList, generation: uiState.selectedGeneration, campusId: uiState.selectedCampus?.id)
                joinLog("filtered for gen \(uiState.selectedGeneration ?? -1), campus \(uiState.selectedCampus?.id ?? -1): \(filtered.count) classes")
                uiState.classes = filtered
                uiState.selectedClass = nil
            case .failure(let error):
                joinLog("loadClasses FAILED: \(error)")
                uiState.isLoadingClasses = false
                uiState.error = toJoinError(error)
            }
        }
    }

    // MARK: - Filter Classes by Generation and Campus
    // Android: filterClasses(source: List<Ban>, generation: Int?)

    private func filterClasses(source: [Ban], generation: Int?, campusId: Int? = nil) -> [Ban] {
        guard let generation = generation else { return [] }
        var filtered = source.filter { $0.generation == generation }
        // 캠퍼스 ID로도 필터링 (API가 전체 반을 반환하는 경우)
        if let campusId = campusId {
            filtered = filtered.filter { $0.resolvedCampusId == campusId }
        }
        return filtered
    }

    // MARK: - Step Navigation

    func goToStep(_ step: Int) {
        uiState.step = step
        uiState.error = nil
    }

    func goBack() -> Bool {
        switch uiState.step {
        case 1:
            return false
        case 2:
            uiState.step = 1
            uiState.error = nil
            uiState.mattermostId = ""
            uiState.verificationCode = ""
            uiState.isCodeSent = false
            uiState.isVerified = false
            uiState.isSendingCode = false
            uiState.isVerifying = false
            uiState.isGenerationDropdownExpanded = false
            return true
        case 3:
            uiState.step = 2
            uiState.error = nil
            uiState.mattermostId = ""
            uiState.verificationCode = ""
            uiState.isCodeSent = false
            uiState.isVerified = false
            uiState.isSendingCode = false
            uiState.isVerifying = false
            uiState.isGenerationDropdownExpanded = false
            uiState.email = ""
            uiState.isCheckingEmail = false
            uiState.isEmailAvailable = nil
            uiState.password = ""
            uiState.passwordConfirm = ""
            uiState.isLoading = false
            return true
        default:
            return false
        }
    }

    // MARK: - Step 1: Personal Info

    func onNameChange(_ value: String) {
        uiState.name = value
    }

    func onStudentIdChange(_ value: String) {
        uiState.studentId = value.filter { $0.isNumber }
    }

    // MARK: - 기수 선택 (Generation)

    func onGenerationDropdownClick() {
        joinLog("onGenerationDropdownClick()")
        uiState.isGenerationDropdownExpanded = true
    }

    func onGenerationDropdownDismiss() {
        joinLog("onGenerationDropdownDismiss()")
        uiState.isGenerationDropdownExpanded = false
    }

    func onGenerationSelected(_ generation: Int) {
        joinLog("onGenerationSelected(\(generation))")
        joinLog("campusClasses count: \(uiState.campusClasses.count)")
        // Android와 동일: 기수 선택 시 campusClasses에서 필터링
        let filteredClasses = filterClasses(source: uiState.campusClasses, generation: generation, campusId: uiState.selectedCampus?.id)
        joinLog("filteredClasses count: \(filteredClasses.count)")
        uiState.selectedGeneration = generation
        uiState.isGenerationDropdownExpanded = false
        uiState.classes = filteredClasses
        uiState.selectedClass = nil
        joinLog("after onGenerationSelected - selectedGeneration: \(uiState.selectedGeneration ?? -1), campus: \(uiState.selectedCampus?.name ?? "nil"), classes: \(uiState.classes.count)")
    }

    // MARK: - 캠퍼스 선택 (Campus)

    func onCampusDropdownClick() {
        joinLog("onCampusDropdownClick() - campuses: \(uiState.campuses.count)")
        uiState.isCampusDropdownExpanded = true
    }

    func onCampusDropdownDismiss() {
        joinLog("onCampusDropdownDismiss()")
        uiState.isCampusDropdownExpanded = false
    }

    func onCampusSelected(_ campus: Campus) {
        joinLog("onCampusSelected(\(campus.name))")
        uiState.selectedCampus = campus
        uiState.isCampusDropdownExpanded = false
        uiState.selectedClass = nil
        uiState.classes = []
        uiState.campusClasses = []
        loadClasses(campusId: campus.id)
    }

    // MARK: - 반 선택 (Class)

    func onClassDropdownClick() {
        joinLog("onClassDropdownClick() - campus: \(uiState.selectedCampus?.name ?? "nil"), gen: \(uiState.selectedGeneration ?? -1), classes: \(uiState.classes.count)")
        // Android와 동일: 캠퍼스와 기수가 선택되어야 클릭 가능
        if uiState.selectedCampus != nil && uiState.selectedGeneration != nil {
            uiState.isClassDropdownExpanded = true
        }
    }

    func onClassDropdownDismiss() {
        joinLog("onClassDropdownDismiss()")
        uiState.isClassDropdownExpanded = false
    }

    func onClassSelected(_ ban: Ban) {
        joinLog("onClassSelected(\(ban.name))")
        uiState.selectedClass = ban
        uiState.isClassDropdownExpanded = false
    }

    // MARK: - Step 2: Mattermost Verification

    func onMattermostIdChange(_ value: String) {
        uiState.mattermostId = value
        uiState.error = nil
    }

    func onVerificationCodeChange(_ value: String) {
        uiState.verificationCode = String(value.filter { $0.isNumber }.prefix(6))
        uiState.error = nil
    }

    func sendVerificationCode() {
        let mmId = uiState.mattermostId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let generation = uiState.selectedGeneration, !mmId.isEmpty else {
            joinLog("sendVerificationCode() - guard failed: mmId='\(uiState.mattermostId)', generation=\(uiState.selectedGeneration ?? -1)")
            return
        }

        joinLog("sendVerificationCode() - mmId='\(mmId)', generation=\(generation), name='\(uiState.name)'")

        Task {
            uiState.isSendingCode = true
            uiState.error = nil

            let result = await authRepository.requestSsafyVerification(
                mattermostId: mmId,
                generation: generation,
                name: uiState.name
            )

            switch result {
            case .success:
                joinLog("sendVerificationCode() SUCCESS")
                uiState.isSendingCode = false
                uiState.isCodeSent = true
            case .failure(let error):
                joinLog("sendVerificationCode() FAILED: \(error)")
                uiState.isSendingCode = false
                uiState.error = toJoinError(error)
            }
        }
    }

    func verifyCode() {
        let mmId = uiState.mattermostId.trimmingCharacters(in: .whitespacesAndNewlines)
        let code = uiState.verificationCode
        guard code.count == 6 else { return }

        Task {
            uiState.isVerifying = true
            uiState.error = nil

            let result = await authRepository.confirmSsafyVerification(mattermostId: mmId, code: code)

            switch result {
            case .success:
                uiState.isVerifying = false
                uiState.isVerified = true
                uiState.step = 3
            case .failure(let error):
                uiState.isVerifying = false
                uiState.error = toJoinError(error)
            }
        }
    }

    // MARK: - Step 3: Account Info

    func onEmailChange(_ value: String) {
        uiState.email = value
        uiState.error = nil
        uiState.isEmailAvailable = nil
    }

    func checkEmailDuplicate() {
        let email = uiState.email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !email.isEmpty else { return }

        Task {
            uiState.isCheckingEmail = true
            uiState.error = nil

            let result = await authRepository.isEmailUsed(email: email)

            switch result {
            case .success(let isUsed):
                uiState.isCheckingEmail = false
                uiState.isEmailAvailable = !isUsed
            case .failure(let error):
                uiState.isCheckingEmail = false
                uiState.isEmailAvailable = nil
                uiState.error = toJoinError(error)
            }
        }
    }

    func onPasswordChange(_ value: String) {
        uiState.password = value
        uiState.error = nil
    }

    func onPasswordConfirmChange(_ value: String) {
        uiState.passwordConfirm = value
        uiState.error = nil
    }

    func signUp(onSuccess: @escaping () -> Void) {
        guard uiState.isStep3Valid else { return }
        guard let campus = uiState.selectedCampus,
              let generation = uiState.selectedGeneration,
              let ban = uiState.selectedClass else { return }

        Task {
            uiState.isLoading = true
            uiState.error = nil

            let info = SignUpInfo(
                email: uiState.email.trimmingCharacters(in: .whitespacesAndNewlines),
                password: uiState.password,
                name: uiState.name.trimmingCharacters(in: .whitespacesAndNewlines),
                studentId: uiState.studentId,
                campus: String(campus.id),
                generation: generation,
                clazz: ban.classNo ?? 0,
                mattermostId: uiState.mattermostId.trimmingCharacters(in: .whitespacesAndNewlines)
            )

            let result = await authRepository.signUp(info: info)

            switch result {
            case .success:
                uiState.isLoading = false
                uiState.isJoinSuccess = true
                onSuccess()
            case .failure(let error):
                uiState.isLoading = false
                uiState.error = toJoinError(error)
            }
        }
    }

    // MARK: - Helpers

    private func toJoinError(_ error: Error) -> JoinError {
        let message = error.localizedDescription
        if message.lowercased().contains("network") {
            return .network
        } else if message.contains("500") {
            return .server(message)
        } else {
            return .validation(message)
        }
    }

    func clearError() {
        uiState.error = nil
    }
}
