import Foundation

// MARK: - Home Service Protocol

protocol HomeService {
    func getHome() async throws -> HomeResponse
}

// MARK: - Home Service Implementation

final class HomeServiceImpl: HomeService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getHome() async throws -> HomeResponse {
        let endpoint = APIEndpoint(path: "/api/home", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
