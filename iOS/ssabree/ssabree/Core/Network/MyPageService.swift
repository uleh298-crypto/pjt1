import Foundation

protocol MyPageService {
    func getMyPage() async throws -> MyPageResponse
    func getMyPosts() async throws -> [PostResponse]
    func getMyComments() async throws -> [MyCommentResponse]
    func getMyScraps() async throws -> [PostResponse]
    func updateProfile(request: UpdateProfileRequest) async throws -> UpdateProfileResponse
    func getAnon() async throws -> AnonResponse
}

final class MyPageServiceImpl: MyPageService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getMyPage() async throws -> MyPageResponse {
        let endpoint = APIEndpoint(path: "/api/members/mypage", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMyPosts() async throws -> [PostResponse] {
        let endpoint = APIEndpoint(path: "/api/members/mypage/posts", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMyComments() async throws -> [MyCommentResponse] {
        let endpoint = APIEndpoint(path: "/api/members/mypage/comments", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMyScraps() async throws -> [PostResponse] {
        let endpoint = APIEndpoint(path: "/api/members/mypage/scraps", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func updateProfile(request: UpdateProfileRequest) async throws -> UpdateProfileResponse {
        let endpoint = APIEndpoint(path: "/api/members/me", method: .PUT, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func getAnon() async throws -> AnonResponse {
        let endpoint = APIEndpoint(path: "/api/member/anon", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
