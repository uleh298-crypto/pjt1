import Foundation
import Observation

struct GroupApplyUiState {
    var isSubmitting: Bool = false
    var error: String? = nil
    var isSuccess: Bool = false
    var portfolioId: Int? = nil
    var portfolioSummary: MyPagePortfolioSummaryModel? = nil
    var isLoadingPortfolio: Bool = false
}

@Observable
final class GroupApplyViewModel {
    private let groupRepository: GroupRepository
    private let portfolioRepository: PortfolioRepository
    private let myPageRepository: MyPageRepository
    var uiState = GroupApplyUiState()

    init(
        groupRepository: GroupRepository,
        portfolioRepository: PortfolioRepository,
        myPageRepository: MyPageRepository
    ) {
        self.groupRepository = groupRepository
        self.portfolioRepository = portfolioRepository
        self.myPageRepository = myPageRepository
    }

    @MainActor
    func loadPortfolio() async {
        uiState.isLoadingPortfolio = true

        // 포트폴리오 ID 가져오기
        let portfolioResult = await portfolioRepository.getMyPortfolio()
        switch portfolioResult {
        case .success(let portfolio):
            uiState.portfolioId = portfolio.id
        case .failure:
            uiState.portfolioId = nil
        }

        // 마이페이지에서 포트폴리오 요약 정보 가져오기
        let myPageResult = await myPageRepository.getMyPage()
        switch myPageResult {
        case .success(let myPage):
            uiState.portfolioSummary = myPage.portfolioSummary
        case .failure:
            uiState.portfolioSummary = nil
        }

        uiState.isLoadingPortfolio = false
    }

    @MainActor
    func applyGroup(
        groupId: Int,
        groupKind: GroupKind,
        title: String,
        message: String,
        position: String
    ) async {
        guard let portfolioId = uiState.portfolioId else {
            uiState.error = "포트폴리오를 먼저 등록해주세요."
            return
        }

        uiState.isSubmitting = true
        uiState.error = nil

        let result: Result<Void, Error>
        if groupKind == .study {
            result = await groupRepository.applyStudy(
                studyId: groupId,
                portfolioId: portfolioId,
                title: title,
                message: message,
                position: position
            )
        } else {
            result = await groupRepository.applyTeam(
                teamId: groupId,
                portfolioId: portfolioId,
                title: title,
                message: message,
                position: position
            )
        }

        switch result {
        case .success:
            uiState.isSuccess = true
        case .failure(let error):
            uiState.error = error.localizedDescription
        }

        uiState.isSubmitting = false
    }

    @MainActor
    func resetResult() {
        uiState.isSuccess = false
        uiState.error = nil
    }
}
