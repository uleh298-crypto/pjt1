import Foundation

// MARK: - Server Error Response

struct ServerErrorResponse: Decodable {
    let code: String?
    let error: String?
    let message: String?
    let status: Int?
    let timestamp: String?
}

// MARK: - API Error Types

enum APIError: Error, LocalizedError {
    case invalidURL
    case encodingFailed
    case decodingFailed(Error)
    case networkError(Error)
    case httpError(statusCode: Int, data: Data?)
    case serverMessage(String)
    case unauthorized
    case forbidden
    case notFound
    case serverError
    case tokenRefreshFailed
    case unknown

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "잘못된 URL입니다"
        case .encodingFailed:
            return "요청 인코딩에 실패했습니다"
        case .decodingFailed(let error):
            return "응답 디코딩에 실패했습니다: \(error.localizedDescription)"
        case .networkError(let error):
            return "네트워크 오류: \(error.localizedDescription)"
        case .httpError(let statusCode, _):
            return "HTTP 오류: \(statusCode)"
        case .serverMessage(let message):
            return message
        case .unauthorized:
            return "인증이 필요합니다"
        case .forbidden:
            return "접근 권한이 없습니다"
        case .notFound:
            return "요청한 리소스를 찾을 수 없습니다"
        case .serverError:
            return "서버 오류가 발생했습니다"
        case .tokenRefreshFailed:
            return "토큰 갱신에 실패했습니다"
        case .unknown:
            return "알 수 없는 오류가 발생했습니다"
        }
    }
}

// MARK: - HTTP Method

enum HTTPMethod: String {
    case GET
    case POST
    case PUT
    case DELETE
    case PATCH
}

// MARK: - API Endpoint

struct APIEndpoint {
    let path: String
    let method: HTTPMethod
    let requiresAuth: Bool

    init(path: String, method: HTTPMethod = .GET, requiresAuth: Bool = true) {
        self.path = path
        self.method = method
        self.requiresAuth = requiresAuth
    }
}

// MARK: - Auth Event Bus (for force logout)

enum AuthEvent {
    case forceLogout
}

@MainActor
final class AuthEventBus {
    static let shared = AuthEventBus()

    private var listeners: [(AuthEvent) -> Void] = []

    private init() {}

    func addListener(_ listener: @escaping (AuthEvent) -> Void) {
        listeners.append(listener)
    }

    func send(_ event: AuthEvent) {
        listeners.forEach { $0(event) }
    }

    func clearListeners() {
        listeners.removeAll()
    }
}

// MARK: - API Client Protocol

protocol APIClientProtocol {
    func request<T: Decodable>(
        endpoint: APIEndpoint,
        body: Encodable?,
        queryItems: [URLQueryItem]?
    ) async throws -> T

    func requestEmpty(
        endpoint: APIEndpoint,
        body: Encodable?,
        queryItems: [URLQueryItem]?
    ) async throws

    func upload<T: Decodable>(
        endpoint: APIEndpoint,
        fileData: Data,
        fileName: String,
        mimeType: String,
        fieldName: String
    ) async throws -> T
}

// MARK: - API Client Implementation

final class APIClient: APIClientProtocol {
    static let shared = APIClient()

    static let baseURL = "http://localhost:8080"

    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    private var authDataStore: AuthDataStore?
    private var isRefreshing = false
    private var refreshTask: Task<Bool, Error>?

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        self.session = URLSession(configuration: configuration)

        self.decoder = JSONDecoder()
        // 백엔드가 camelCase를 반환하므로 snake_case 변환하지 않음 (CodingKeys로 개별 처리)

        self.encoder = JSONEncoder()
        // 백엔드가 camelCase를 기대하므로 snake_case 변환하지 않음
    }

    func configure(authDataStore: AuthDataStore) {
        self.authDataStore = authDataStore
    }

    // MARK: - Request with Response

    func request<T: Decodable>(
        endpoint: APIEndpoint,
        body: Encodable? = nil,
        queryItems: [URLQueryItem]? = nil
    ) async throws -> T {
        let data = try await performRequest(endpoint: endpoint, body: body, queryItems: queryItems)

        do {
            let decoded = try decoder.decode(T.self, from: data)
            return decoded
        } catch {
            print("Decoding error: \(error)")
            print("Response data: \(String(data: data, encoding: .utf8) ?? "nil")")
            throw APIError.decodingFailed(error)
        }
    }

    // MARK: - Request without Response (Empty)

    func requestEmpty(
        endpoint: APIEndpoint,
        body: Encodable? = nil,
        queryItems: [URLQueryItem]? = nil
    ) async throws {
        _ = try await performRequest(endpoint: endpoint, body: body, queryItems: queryItems)
    }

    // MARK: - File Upload

    func upload<T: Decodable>(
        endpoint: APIEndpoint,
        fileData: Data,
        fileName: String,
        mimeType: String,
        fieldName: String = "file"
    ) async throws -> T {
        guard var urlComponents = URLComponents(string: APIClient.baseURL + endpoint.path) else {
            throw APIError.invalidURL
        }

        guard let url = urlComponents.url else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue

        // Add auth header
        if endpoint.requiresAuth {
            if let token = authDataStore?.getAccessToken(),
               let tokenType = authDataStore?.getTokenType() {
                request.setValue("\(tokenType) \(token)", forHTTPHeaderField: "Authorization")
            }
        }

        // Create multipart form data
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var bodyData = Data()
        bodyData.append("--\(boundary)\r\n".data(using: .utf8)!)
        bodyData.append("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        bodyData.append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        bodyData.append(fileData)
        bodyData.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)

        request.httpBody = bodyData

        // Use longer timeout for uploads
        let uploadConfiguration = URLSessionConfiguration.default
        uploadConfiguration.timeoutIntervalForRequest = 120
        uploadConfiguration.timeoutIntervalForResource = 300
        let uploadSession = URLSession(configuration: uploadConfiguration)

        do {
            let (data, response) = try await uploadSession.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.unknown
            }

            try handleHTTPResponse(httpResponse, data: data, endpoint: endpoint)

            return try decoder.decode(T.self, from: data)
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }

    // MARK: - Private Methods

    private func performRequest(
        endpoint: APIEndpoint,
        body: Encodable?,
        queryItems: [URLQueryItem]?
    ) async throws -> Data {
        let request = try buildRequest(endpoint: endpoint, body: body, queryItems: queryItems)

        do {
            let (data, response) = try await session.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.unknown
            }

            // Handle 401 Unauthorized - try token refresh
            if httpResponse.statusCode == 401 && endpoint.requiresAuth {
                let refreshSuccess = try await refreshTokenIfNeeded(endpoint: endpoint)
                if refreshSuccess {
                    // Retry original request with new token
                    let newRequest = try buildRequest(endpoint: endpoint, body: body, queryItems: queryItems)
                    let (newData, newResponse) = try await session.data(for: newRequest)

                    guard let newHttpResponse = newResponse as? HTTPURLResponse else {
                        throw APIError.unknown
                    }

                    try handleHTTPResponse(newHttpResponse, data: newData, endpoint: endpoint)
                    return newData
                } else {
                    // Token refresh failed - force logout
                    await handleForceLogout()
                    throw APIError.unauthorized
                }
            }

            try handleHTTPResponse(httpResponse, data: data, endpoint: endpoint)
            return data

        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }

    private func buildRequest(
        endpoint: APIEndpoint,
        body: Encodable?,
        queryItems: [URLQueryItem]?
    ) throws -> URLRequest {
        guard var urlComponents = URLComponents(string: APIClient.baseURL + endpoint.path) else {
            throw APIError.invalidURL
        }

        if let queryItems = queryItems, !queryItems.isEmpty {
            urlComponents.queryItems = queryItems
        }

        guard let url = urlComponents.url else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        // Add Authorization header if required
        if endpoint.requiresAuth {
            if let token = authDataStore?.getAccessToken(),
               let tokenType = authDataStore?.getTokenType() {
                request.setValue("\(tokenType) \(token)", forHTTPHeaderField: "Authorization")
            }
        }

        // Add body if present
        if let body = body {
            do {
                request.httpBody = try encoder.encode(body)
            } catch {
                throw APIError.encodingFailed
            }
        }

        return request
    }

    private func handleHTTPResponse(
        _ response: HTTPURLResponse,
        data: Data,
        endpoint: APIEndpoint
    ) throws {
        switch response.statusCode {
        case 200...299:
            return // Success
        case 400:
            // Bad Request - 서버 에러 메시지 파싱 시도
            if let errorResponse = try? decoder.decode(ServerErrorResponse.self, from: data),
               let message = errorResponse.message {
                print("[APIClient] Server error: code=\(errorResponse.code ?? ""), message=\(message)")
                throw APIError.serverMessage(message)
            }
            throw APIError.httpError(statusCode: response.statusCode, data: data)
        case 401:
            // Unauthorized - 서버 에러 메시지 파싱 시도
            if let errorResponse = try? decoder.decode(ServerErrorResponse.self, from: data),
               let message = errorResponse.message {
                print("[APIClient] Auth error: code=\(errorResponse.code ?? ""), message=\(message)")
                throw APIError.serverMessage(message)
            }
            throw APIError.unauthorized
        case 403:
            throw APIError.forbidden
        case 404:
            throw APIError.notFound
        case 500...599:
            throw APIError.serverError
        default:
            // 기타 에러도 서버 메시지 파싱 시도
            if let errorResponse = try? decoder.decode(ServerErrorResponse.self, from: data),
               let message = errorResponse.message {
                print("[APIClient] Server error: status=\(response.statusCode), message=\(message)")
                throw APIError.serverMessage(message)
            }
            throw APIError.httpError(statusCode: response.statusCode, data: data)
        }
    }

    // MARK: - Token Refresh

    private func refreshTokenIfNeeded(endpoint: APIEndpoint) async throws -> Bool {
        // Skip refresh for auth endpoints
        if endpoint.path.contains("/api/auth/login") || endpoint.path.contains("/api/auth/refresh") {
            return false
        }

        // If already refreshing, wait for it
        if let existingTask = refreshTask {
            return try await existingTask.value
        }

        // Start new refresh task
        refreshTask = Task {
            defer { refreshTask = nil }
            return try await performTokenRefresh()
        }

        return try await refreshTask!.value
    }

    private func performTokenRefresh() async throws -> Bool {
        guard let refreshToken = authDataStore?.getRefreshToken() else {
            return false
        }

        let endpoint = APIEndpoint(path: "/api/auth/refresh", method: .POST, requiresAuth: false)

        guard let url = URL(string: APIClient.baseURL + endpoint.path) else {
            return false
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        struct RefreshRequest: Encodable {
            let refreshToken: String
        }

        do {
            request.httpBody = try encoder.encode(RefreshRequest(refreshToken: refreshToken))

            let (data, response) = try await session.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                return false
            }

            guard httpResponse.statusCode == 200 else {
                // Refresh failed - check if should force logout
                if httpResponse.statusCode == 401 || httpResponse.statusCode == 403 {
                    await handleForceLogout()
                }
                return false
            }

            let tokenResponse = try decoder.decode(TokenResponse.self, from: data)

            // Save new tokens
            let newTokens = AuthTokens(
                accessToken: tokenResponse.accessToken,
                refreshToken: tokenResponse.refreshToken,
                tokenType: tokenResponse.grantType ?? "Bearer",
                userId: tokenResponse.userId,
                uid: tokenResponse.uid
            )

            authDataStore?.saveTokens(newTokens)

            return true
        } catch {
            print("Token refresh failed: \(error)")
            return false
        }
    }

    @MainActor
    private func handleForceLogout() {
        authDataStore?.clear()
        AuthEventBus.shared.send(.forceLogout)
    }
}
