import Foundation

// MARK: - MyPage UI State

struct MyPageUiState {
    var isLoading: Bool = false
    var isLoadingPortfolio: Bool = false
    var isUploadingImage: Bool = false
    var errorMessage: String? = nil
    var myPage: MyPageModel? = nil
    var stackImageMap: [String: String] = [:]
}

// MARK: - MyPage ViewModel

@Observable
final class MyPageViewModel {
    private let myPageRepository: MyPageRepository
    private let portfolioRepository: PortfolioRepository?
    private let uploadRepository: UploadRepository?
    private let stackRepository: StackRepository?

    private(set) var uiState = MyPageUiState()

    init(myPageRepository: MyPageRepository, portfolioRepository: PortfolioRepository? = nil, uploadRepository: UploadRepository? = nil, stackRepository: StackRepository? = nil) {
        self.myPageRepository = myPageRepository
        self.portfolioRepository = portfolioRepository
        self.uploadRepository = uploadRepository
        self.stackRepository = stackRepository
    }

    @MainActor
    func loadMyPage() async {
        let hasCached = uiState.myPage != nil
        uiState.isLoading = !hasCached
        uiState.errorMessage = nil

        async let myPageResult = myPageRepository.getMyPage()
        async let stacksResult = loadStackImageMap()
        async let portfoliosResult = loadPortfolios()

        let stackMap = await stacksResult
        let portfolios = await portfoliosResult
        let result = await myPageResult

        switch result {
        case .success(var myPage):
            print("[MyPageViewModel] loadMyPage: succeed")
            // 포트폴리오에서 solved.ac 정보 보강 (백엔드 MyPage API는 solvedAcRank만 반환)
            if let portfolio = portfolios.first {
                myPage = enrichWithPortfolio(myPage: myPage, portfolio: portfolio, allPortfolios: portfolios)
            }
            uiState.myPage = myPage
            uiState.stackImageMap = stackMap
            uiState.isLoading = false
        case .failure(let error):
            print("[MyPageViewModel] loadMyPage: failed (\(error.localizedDescription))")
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    private func loadPortfolios() async -> [PortfolioModel] {
        guard let portfolioRepository = portfolioRepository else { return [] }
        let result = await portfolioRepository.getMyPortfolios()
        switch result {
        case .success(let portfolios): return portfolios
        case .failure: return []
        }
    }

    private func enrichWithPortfolio(myPage: MyPageModel, portfolio: PortfolioModel, allPortfolios: [PortfolioModel]) -> MyPageModel {
        let existing = myPage.portfolioSummary
        let solvedAcInfo = portfolio.solvedAcInfo

        let enrichedSummary = MyPagePortfolioSummaryModel(
            techStack: existing?.techStack ?? [:],
            ssafySwRating: existing?.ssafySwRating ?? portfolio.swTestRank,
            solvedAcRank: existing?.solvedAcRank,
            solvedAcHandle: portfolio.bojHandle,
            solvedAcTierName: solvedAcInfo?.tierName,
            solvedAcTierImageUrl: solvedAcInfo?.tierImageUrl,
            solvedAcSolvedCount: solvedAcInfo?.solvedCount,
            links: existing?.links ?? portfolio.urls.map { $0.url },
            projects: existing?.projects ?? allPortfolios.map { $0.title }
        )

        return MyPageModel(
            user: myPage.user,
            counts: myPage.counts,
            portfolioSummary: enrichedSummary
        )
    }

    private func loadStackImageMap() async -> [String: String] {
        guard let stackRepository = stackRepository else { return [:] }
        let result = await stackRepository.getStacks()
        switch result {
        case .success(let stacks):
            var map: [String: String] = [:]
            for stack in stacks {
                if let imgUrl = stack.imgUrl, !imgUrl.isEmpty {
                    map[stack.name] = imgUrl
                }
            }
            return map
        case .failure:
            return [:]
        }
    }

    @MainActor
    func uploadProfileImage(imageData: Data) async {
        guard let uploadRepository = uploadRepository else { return }

        uiState.isUploadingImage = true
        uiState.errorMessage = nil

        let uploadResult = await uploadRepository.uploadImage(image: imageData)

        switch uploadResult {
        case .success(let imageUrl):
            // 프로필 이미지 URL 업데이트
            let updateResult = await myPageRepository.updateProfileImage(imageUrl)
            switch updateResult {
            case .success:
                print("[MyPageViewModel] uploadProfileImage: succeed")
                await loadMyPage()
            case .failure(let error):
                print("[MyPageViewModel] updateProfileImage: failed (\(error.localizedDescription))")
                uiState.errorMessage = error.localizedDescription
            }
        case .failure(let error):
            print("[MyPageViewModel] uploadProfileImage: failed (\(error.localizedDescription))")
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isUploadingImage = false
    }

    @MainActor
    func deleteProfileImage() async {
        uiState.isUploadingImage = true

        let result = await myPageRepository.updateProfileImage("")

        switch result {
        case .success:
            print("[MyPageViewModel] deleteProfileImage: succeed")
            await loadMyPage()
        case .failure(let error):
            print("[MyPageViewModel] deleteProfileImage: failed (\(error.localizedDescription))")
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isUploadingImage = false
    }

    @MainActor
    func setError(_ message: String) {
        uiState.errorMessage = message
    }
}
