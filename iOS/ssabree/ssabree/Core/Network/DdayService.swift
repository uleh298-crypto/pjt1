import Foundation

protocol DdayService {
    func getDdays() async throws -> DdayListResponse
}

final class DdayServiceImpl: DdayService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getDdays() async throws -> DdayListResponse {
        let endpoint = APIEndpoint(path: "/api/ddays", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
