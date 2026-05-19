import Foundation

// MARK: - Portfolio Models

struct PortfolioModel: Equatable {
    let id: Int
    let memberId: Int
    let memberName: String
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let solvedAcInfo: SolvedAcInfoModel?
    let swTestRank: String?
    let isVisible: Bool
    let createdAt: String?
    let updatedAt: String?
    let stacks: [PortfolioStackModel]
    let urls: [PortfolioUrlModel]
    let images: [PortfolioImageModel]
}

struct SolvedAcInfoModel: Equatable {
    let tier: Int?
    let tierName: String?
    let tierImageUrl: String?
    let rating: Int?
    let solvedCount: Int?
    let rank: Int?
}

struct PortfolioStackModel: Equatable {
    let id: Int
    let stackId: Int
    let stackName: String
    let stackImgUrl: String?
    let expertLevel: String?

    var expertLevelLabel: String {
        switch expertLevel?.lowercased() {
        case "high": return "상"
        case "mid": return "중"
        case "low": return "하"
        default: return "중"
        }
    }
}

struct PortfolioUrlModel: Equatable {
    let id: Int
    let url: String
}

struct PortfolioImageModel: Equatable {
    let id: Int
    let imageUrl: String
    let orders: Int
}

// MARK: - Portfolio Update Info Models

struct PortfolioCreateInfo {
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let swTestRank: String?
    let isVisible: Bool
    let stacks: [PortfolioStackUpdateInfo]
    let urls: [PortfolioUrlUpdateInfo]
    let images: [PortfolioImageUpdateInfo]
}

struct PortfolioUpdateInfo {
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let swTestRank: String?
    let isVisible: Bool
    let stacks: [PortfolioStackUpdateInfo]
    let urls: [PortfolioUrlUpdateInfo]
    let images: [PortfolioImageUpdateInfo]
}

struct PortfolioStackUpdateInfo {
    let stackId: Int
    let expertLevel: String
}

struct PortfolioUrlUpdateInfo {
    let url: String
}

struct PortfolioImageUpdateInfo {
    let imageUrl: String
    let orders: Int
}

struct SolvedacVerifyInfo {
    let handle: String?
    let tier: Int
    let rating: Int?
    let solvedCount: Int
    let rank: Int?
}

// MARK: - Portfolio Summary Model

struct PortfolioSummaryModel {
    let id: Int
    let summary: String?
}

// MARK: - Portfolio Repository Protocol

protocol PortfolioRepository {
    func getMyPortfolios() async -> Result<[PortfolioModel], Error>
    func getMyPortfolio() async -> Result<PortfolioSummaryModel, Error>
    func getPortfolio(id: Int) async -> Result<PortfolioModel, Error>
    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error>
    func createPortfolio(info: PortfolioCreateInfo) async -> Result<Int, Error>
    func updatePortfolio(id: Int, info: PortfolioUpdateInfo) async -> Result<Int, Error>
    func verifySolvedac(handle: String) async -> Result<SolvedacVerifyInfo, Error>
}

// MARK: - Portfolio Repository Implementation

final class PortfolioRepositoryImpl: PortfolioRepository {
    private let portfolioService: PortfolioService
    private let projectService: ProjectService

    init(portfolioService: PortfolioService, projectService: ProjectService? = nil) {
        self.portfolioService = portfolioService
        self.projectService = projectService ?? ProjectServiceImpl()
    }

    func getMyPortfolios() async -> Result<[PortfolioModel], Error> {
        do {
            let response = try await portfolioService.getMyPortfolios()
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getMyPortfolio() async -> Result<PortfolioSummaryModel, Error> {
        do {
            let response = try await portfolioService.getMyPortfolios()
            if let first = response.first {
                return .success(PortfolioSummaryModel(id: first.id, summary: first.introduction))
            }
            return .failure(NSError(domain: "", code: 404, userInfo: [NSLocalizedDescriptionKey: "포트폴리오가 없습니다"]))
        } catch {
            return .failure(error)
        }
    }

    func getPortfolio(id: Int) async -> Result<PortfolioModel, Error> {
        do {
            let response = try await portfolioService.getPortfolio(id: id)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        do {
            let response = try await projectService.getProjectsByPortfolio(portfolioId: portfolioId)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func createPortfolio(info: PortfolioCreateInfo) async -> Result<Int, Error> {
        do {
            let request = PortfolioCreateRequest(
                title: info.title,
                description: info.description,
                introduction: info.introduction,
                bojHandle: info.bojHandle,
                swTestRank: info.swTestRank,
                isVisible: info.isVisible,
                stacks: info.stacks.map { PortfolioStackRequest(stackId: $0.stackId, expertLevel: $0.expertLevel) },
                urls: info.urls.map { PortfolioUrlRequest(url: $0.url) },
                images: info.images.map { PortfolioImageRequest(imageUrl: $0.imageUrl, orders: $0.orders) }
            )
            let portfolioId = try await portfolioService.createPortfolio(request)
            return .success(portfolioId)
        } catch {
            return .failure(error)
        }
    }

    func updatePortfolio(id: Int, info: PortfolioUpdateInfo) async -> Result<Int, Error> {
        do {
            let request = PortfolioUpdateRequest(
                title: info.title,
                description: info.description,
                introduction: info.introduction,
                bojHandle: info.bojHandle,
                swTestRank: info.swTestRank,
                isVisible: info.isVisible,
                stacks: info.stacks.map { PortfolioStackRequest(stackId: $0.stackId, expertLevel: $0.expertLevel) },
                urls: info.urls.map { PortfolioUrlRequest(url: $0.url) },
                images: info.images.map { PortfolioImageRequest(imageUrl: $0.imageUrl, orders: $0.orders) }
            )
            let portfolioId = try await portfolioService.updatePortfolio(id: id, request)
            return .success(portfolioId)
        } catch {
            return .failure(error)
        }
    }

    func verifySolvedac(handle: String) async -> Result<SolvedacVerifyInfo, Error> {
        do {
            let response = try await portfolioService.verifySolvedac(handle: handle)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Fake Portfolio Repository

final class FakePortfolioRepository: PortfolioRepository {
    func getMyPortfolios() async -> Result<[PortfolioModel], Error> {
        .success([
            PortfolioModel(
                id: 1,
                memberId: 1,
                memberName: "김싸피",
                title: "백엔드 개발자 포트폴리오",
                description: "Spring Boot와 Kotlin을 활용한 백엔드 개발",
                introduction: "안녕하세요. 백엔드 개발자 김싸피입니다.",
                bojHandle: "example123",
                solvedAcInfo: SolvedAcInfoModel(
                    tier: 15,
                    tierName: "Gold I",
                    tierImageUrl: "https://static.solved.ac/tier_small/15.svg",
                    rating: 1600,
                    solvedCount: 150,
                    rank: 10000
                ),
                swTestRank: "A+",
                isVisible: true,
                createdAt: "2024-01-01T00:00:00",
                updatedAt: "2024-01-15T00:00:00",
                stacks: [
                    PortfolioStackModel(id: 1, stackId: 1, stackName: "Swift", stackImgUrl: nil, expertLevel: "high"),
                    PortfolioStackModel(id: 2, stackId: 2, stackName: "Kotlin", stackImgUrl: nil, expertLevel: "mid")
                ],
                urls: [
                    PortfolioUrlModel(id: 1, url: "https://github.com/ssafy")
                ],
                images: []
            )
        ])
    }

    func getMyPortfolio() async -> Result<PortfolioSummaryModel, Error> {
        .success(PortfolioSummaryModel(id: 1, summary: "안녕하세요. 백엔드 개발자 김싸피입니다."))
    }

    func getPortfolio(id: Int) async -> Result<PortfolioModel, Error> {
        let result = await getMyPortfolios()
        switch result {
        case .success(let portfolios):
            if let portfolio = portfolios.first {
                return .success(portfolio)
            }
            return .failure(NSError(domain: "", code: 404, userInfo: [NSLocalizedDescriptionKey: "Portfolio not found"]))
        case .failure(let error):
            return .failure(error)
        }
    }

    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        .success([
            ProjectModel(
                id: 1,
                title: "SSABREE",
                introduction: "싸피 커뮤니티 앱",
                description: "싸피 교육생들을 위한 커뮤니티 서비스입니다.",
                techStacks: ["Swift", "SwiftUI", "Kotlin", "Jetpack Compose"],
                urls: ["https://github.com/ssafy/ssabree"],
                imageUrls: [],
                createdAt: "2024-01-01T00:00:00",
                updatedAt: "2024-01-15T00:00:00",
                portfolioId: portfolioId
            )
        ])
    }

    func createPortfolio(info: PortfolioCreateInfo) async -> Result<Int, Error> {
        .success(1)
    }

    func updatePortfolio(id: Int, info: PortfolioUpdateInfo) async -> Result<Int, Error> {
        .success(id)
    }

    func verifySolvedac(handle: String) async -> Result<SolvedacVerifyInfo, Error> {
        .success(SolvedacVerifyInfo(handle: handle, tier: 15, rating: 1600, solvedCount: 150, rank: 10000))
    }
}

// MARK: - Response to Model Extensions

extension PortfolioResponse {
    func toModel() -> PortfolioModel {
        PortfolioModel(
            id: id,
            memberId: memberId,
            memberName: memberName,
            title: title,
            description: description,
            introduction: introduction,
            bojHandle: bojHandle,
            solvedAcInfo: solvedAcInfo?.toModel(),
            swTestRank: swTestRank,
            isVisible: isVisible,
            createdAt: createdAt,
            updatedAt: updatedAt,
            stacks: stacks?.map { $0.toModel() } ?? [],
            urls: urls?.map { $0.toModel() } ?? [],
            images: images?.map { $0.toModel() } ?? []
        )
    }
}

extension SolvedAcInfoResponse {
    func toModel() -> SolvedAcInfoModel {
        SolvedAcInfoModel(
            tier: tier,
            tierName: tierName,
            tierImageUrl: tierImageUrl,
            rating: rating,
            solvedCount: solvedCount,
            rank: rank
        )
    }
}

extension PortfolioStackResponse {
    func toModel() -> PortfolioStackModel {
        PortfolioStackModel(
            id: id,
            stackId: stackId,
            stackName: stackName,
            stackImgUrl: stackImgUrl,
            expertLevel: expertLevel
        )
    }
}

extension PortfolioUrlResponse {
    func toModel() -> PortfolioUrlModel {
        PortfolioUrlModel(
            id: id,
            url: url
        )
    }
}

extension PortfolioImageResponse {
    func toModel() -> PortfolioImageModel {
        PortfolioImageModel(
            id: id,
            imageUrl: imageUrl,
            orders: orders
        )
    }
}

extension SolvedacVerifyResponse {
    func toModel() -> SolvedacVerifyInfo {
        SolvedacVerifyInfo(
            handle: handle,
            tier: tier,
            rating: rating,
            solvedCount: solvedCount,
            rank: rank
        )
    }
}
