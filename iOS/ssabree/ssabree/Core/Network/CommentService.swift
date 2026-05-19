import Foundation

struct EmptyResponse: Decodable {}

protocol CommentService {
    func likeComment(commentId: Int) async throws -> CommentLikeResponse
    func unlikeComment(commentId: Int) async throws -> CommentLikeResponse
    func updateComment(commentId: Int, content: String) async throws
    func deleteComment(commentId: Int) async throws
}

struct CommentUpdateRequest: Encodable {
    let content: String
}

final class CommentServiceImpl: CommentService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func likeComment(commentId: Int) async throws -> CommentLikeResponse {
        let endpoint = APIEndpoint(path: "/api/comments/\(commentId)/like", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func unlikeComment(commentId: Int) async throws -> CommentLikeResponse {
        let endpoint = APIEndpoint(path: "/api/comments/\(commentId)/like", method: .DELETE, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func updateComment(commentId: Int, content: String) async throws {
        let endpoint = APIEndpoint(path: "/api/comments/\(commentId)", method: .PUT, requiresAuth: true)
        let request = CommentUpdateRequest(content: content)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteComment(commentId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/comments/\(commentId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
