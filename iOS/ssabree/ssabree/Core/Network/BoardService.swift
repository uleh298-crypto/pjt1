import Foundation

// MARK: - Board Service Protocol

protocol BoardService {
    func getBoards() async throws -> [BoardResponse]
    func getNotice() async throws -> BoardNoticeResponse
}

// MARK: - Board Service Implementation

final class BoardServiceImpl: BoardService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getBoards() async throws -> [BoardResponse] {
        let endpoint = APIEndpoint(path: "/api/boards", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getNotice() async throws -> BoardNoticeResponse {
        let endpoint = APIEndpoint(path: "/api/boards/notice", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
