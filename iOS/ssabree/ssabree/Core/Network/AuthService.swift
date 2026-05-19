import Foundation

// MARK: - Auth Service Protocol

protocol AuthService {
    func login(_ request: LoginRequest) async throws -> TokenResponse
    func refresh(_ request: RefreshRequest) async throws -> TokenResponse
    func requestSsafyVerification(_ request: SsafyVerifyRequest) async throws
    func confirmSsafyVerification(_ request: SsafyConfirmRequest) async throws
    func signUp(_ request: SignUpRequest) async throws
    func checkEmailAvailable(email: String) async throws -> EmailCheckResponse
    func findId(mattermostId: String) async throws -> FindIdResponse
    func resetPassword(_ request: ResetPasswordRequest) async throws -> ResetPasswordResponse
    func deleteMe() async throws
    func getMe() async throws -> MeResponse
}

// MARK: - Auth Service Implementation

final class AuthServiceImpl: AuthService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func login(_ request: LoginRequest) async throws -> TokenResponse {
        let endpoint = APIEndpoint(path: "/api/auth/login", method: .POST, requiresAuth: false)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func refresh(_ request: RefreshRequest) async throws -> TokenResponse {
        let endpoint = APIEndpoint(path: "/api/auth/refresh", method: .POST, requiresAuth: false)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func requestSsafyVerification(_ request: SsafyVerifyRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/auth/send", method: .POST, requiresAuth: false)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func confirmSsafyVerification(_ request: SsafyConfirmRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/auth/verify", method: .POST, requiresAuth: false)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func signUp(_ request: SignUpRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/members/signup", method: .POST, requiresAuth: false)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func checkEmailAvailable(email: String) async throws -> EmailCheckResponse {
        let endpoint = APIEndpoint(path: "/api/members/check-email", method: .GET, requiresAuth: false)
        let queryItems = [URLQueryItem(name: "email", value: email)]
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
    }

    func findId(mattermostId: String) async throws -> FindIdResponse {
        let endpoint = APIEndpoint(path: "/api/auth/findId", method: .GET, requiresAuth: false)
        let queryItems = [URLQueryItem(name: "mattermostId", value: mattermostId)]
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
    }

    func resetPassword(_ request: ResetPasswordRequest) async throws -> ResetPasswordResponse {
        let endpoint = APIEndpoint(path: "/api/auth/resetPassword", method: .POST, requiresAuth: false)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteMe() async throws {
        let endpoint = APIEndpoint(path: "/api/members/me", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMe() async throws -> MeResponse {
        let endpoint = APIEndpoint(path: "/api/members/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
