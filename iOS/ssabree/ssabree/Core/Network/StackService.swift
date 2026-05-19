import Foundation

protocol StackService {
    func getStacks() async throws -> [StackResponse]
}

struct StackResponse: Decodable {
    let id: Int
    let name: String
    let imgUrl: String?
}

final class StackServiceImpl: StackService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getStacks() async throws -> [StackResponse] {
        let endpoint = APIEndpoint(path: "/api/stacks", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
