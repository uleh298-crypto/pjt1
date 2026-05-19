import Foundation

// MARK: - Portfolio Service Protocol

protocol PortfolioService {
    func getMyPortfolios() async throws -> [PortfolioResponse]
    func getPortfolio(id: Int) async throws -> PortfolioResponse
    func createPortfolio(_ request: PortfolioCreateRequest) async throws -> Int
    func updatePortfolio(id: Int, _ request: PortfolioUpdateRequest) async throws -> Int
    func verifySolvedac(handle: String) async throws -> SolvedacVerifyResponse
}

// MARK: - Portfolio Response DTOs

struct PortfolioResponse: Decodable {
    let id: Int
    let memberId: Int
    let memberName: String
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let solvedAcInfo: SolvedAcInfoResponse?
    let swTestRank: String?
    let isVisible: Bool
    let createdAt: String?
    let updatedAt: String?
    let stacks: [PortfolioStackResponse]?
    let urls: [PortfolioUrlResponse]?
    let images: [PortfolioImageResponse]?
}

struct SolvedAcInfoResponse: Decodable {
    let tier: Int?
    let tierName: String?
    let tierImageUrl: String?
    let rating: Int?
    let solvedCount: Int?
    let rank: Int?
}

struct PortfolioStackResponse: Decodable {
    let id: Int
    let stackId: Int
    let stackName: String
    let stackImgUrl: String?
    let expertLevel: String?
}

struct PortfolioUrlResponse: Decodable {
    let id: Int
    let url: String
}

struct PortfolioImageResponse: Decodable {
    let id: Int
    let imageUrl: String
    let orders: Int
}

struct SolvedacVerifyResponse: Decodable {
    let handle: String?
    let tier: Int
    let rating: Int?
    let solvedCount: Int
    let rank: Int?
}

// MARK: - Portfolio Request DTOs

struct PortfolioCreateRequest: Encodable {
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let swTestRank: String?
    let isVisible: Bool
    let stacks: [PortfolioStackRequest]
    let urls: [PortfolioUrlRequest]
    let images: [PortfolioImageRequest]
}

struct PortfolioUpdateRequest: Encodable {
    let title: String
    let description: String
    let introduction: String
    let bojHandle: String?
    let swTestRank: String?
    let isVisible: Bool
    let stacks: [PortfolioStackRequest]
    let urls: [PortfolioUrlRequest]
    let images: [PortfolioImageRequest]
}

struct PortfolioStackRequest: Encodable {
    let stackId: Int
    let expertLevel: String
}

struct PortfolioUrlRequest: Encodable {
    let url: String
}

struct PortfolioImageRequest: Encodable {
    let imageUrl: String
    let orders: Int
}

// MARK: - Portfolio Service Implementation

final class PortfolioServiceImpl: PortfolioService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getMyPortfolios() async throws -> [PortfolioResponse] {
        let endpoint = APIEndpoint(path: "/api/portfolios/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getPortfolio(id: Int) async throws -> PortfolioResponse {
        let endpoint = APIEndpoint(path: "/api/portfolios/\(id)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createPortfolio(_ request: PortfolioCreateRequest) async throws -> Int {
        let endpoint = APIEndpoint(path: "/api/portfolios", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updatePortfolio(id: Int, _ request: PortfolioUpdateRequest) async throws -> Int {
        let endpoint = APIEndpoint(path: "/api/portfolios/\(id)", method: .PUT, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func verifySolvedac(handle: String) async throws -> SolvedacVerifyResponse {
        let endpoint = APIEndpoint(path: "/api/portfolios/solvedac/verify", method: .GET, requiresAuth: true)
        let queryItems = [URLQueryItem(name: "handle", value: handle)]
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
    }
}
