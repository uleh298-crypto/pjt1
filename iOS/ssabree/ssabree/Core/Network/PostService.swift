import Foundation

// MARK: - Post Service Protocol

protocol PostService {
    func getPosts(boardId: Int?, keyword: String?, cursor: String?, limit: Int) async throws -> PagedPostResponse
    func getHotPosts(cursor: String?, limit: Int) async throws -> PagedPostResponse
    func getPost(id: Int) async throws -> PostDetailResponse
    func createPost(_ request: PostCreateRequest) async throws -> PostResponse
    func updatePost(id: Int, _ request: PostUpdateRequest) async throws -> PostResponse
    func deletePost(id: Int) async throws

    func createComment(postId: Int, _ request: CommentCreateRequest) async throws -> CommentResponse
    func createReply(postId: Int, commentId: Int, _ request: ReplyCreateRequest) async throws -> ReplyResponse

    func vote(postId: Int, _ request: VoteRequest) async throws -> PollResponse

    func likePost(postId: Int) async throws -> PostLikeResponse
    func unlikePost(postId: Int) async throws -> PostLikeResponse

    func scrapPost(postId: Int) async throws -> ScrapResponse
    func unscrapPost(postId: Int) async throws -> ScrapResponse
}

// MARK: - Post Service Implementation

final class PostServiceImpl: PostService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getPosts(boardId: Int?, keyword: String?, cursor: String?, limit: Int = 20) async throws -> PagedPostResponse {
        let endpoint = APIEndpoint(path: "/api/posts", method: .GET, requiresAuth: true)

        var queryItems: [URLQueryItem] = []
        if let boardId = boardId {
            queryItems.append(URLQueryItem(name: "boardId", value: "\(boardId)"))
        }
        if let keyword = keyword, !keyword.isEmpty {
            queryItems.append(URLQueryItem(name: "keyword", value: keyword))
        }
        if let cursor = cursor {
            queryItems.append(URLQueryItem(name: "cursor", value: cursor))
        }
        queryItems.append(URLQueryItem(name: "limit", value: "\(limit)"))

        print("[PostService] getPosts - boardId: \(boardId ?? -1), cursor: \(cursor ?? "nil"), limit: \(limit)")
        let result: PagedPostResponse = try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
        print("[PostService] getPosts - received \(result.posts.count) posts, hasNext: \(result.hasNext)")
        return result
    }

    func getHotPosts(cursor: String?, limit: Int = 20) async throws -> PagedPostResponse {
        let endpoint = APIEndpoint(path: "/api/posts/hot", method: .GET, requiresAuth: true)

        var queryItems: [URLQueryItem] = []
        if let cursor = cursor {
            queryItems.append(URLQueryItem(name: "cursor", value: cursor))
        }
        queryItems.append(URLQueryItem(name: "limit", value: "\(limit)"))

        print("[PostService] getHotPosts - cursor: \(cursor ?? "nil"), limit: \(limit)")
        let result: PagedPostResponse = try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
        print("[PostService] getHotPosts - received \(result.posts.count) posts, hasNext: \(result.hasNext)")
        return result
    }

    func getPost(id: Int) async throws -> PostDetailResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(id)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createPost(_ request: PostCreateRequest) async throws -> PostResponse {
        let endpoint = APIEndpoint(path: "/api/posts", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updatePost(id: Int, _ request: PostUpdateRequest) async throws -> PostResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(id)", method: .PUT, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deletePost(id: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/posts/\(id)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createComment(postId: Int, _ request: CommentCreateRequest) async throws -> CommentResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/comments", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func createReply(postId: Int, commentId: Int, _ request: ReplyCreateRequest) async throws -> ReplyResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/comments/\(commentId)/replies", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func vote(postId: Int, _ request: VoteRequest) async throws -> PollResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/poll/vote", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func likePost(postId: Int) async throws -> PostLikeResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/like", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func unlikePost(postId: Int) async throws -> PostLikeResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/like", method: .DELETE, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func scrapPost(postId: Int) async throws -> ScrapResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/scrap", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func unscrapPost(postId: Int) async throws -> ScrapResponse {
        let endpoint = APIEndpoint(path: "/api/posts/\(postId)/scrap", method: .DELETE, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
