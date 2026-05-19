import Foundation

protocol NotificationService {
    func getNotifications() async throws -> [NotificationResponse]
    func markAsRead(id: Int) async throws
    func registerToken(token: String) async throws
    func subscribe(token: String) async throws
    func unsubscribe(token: String) async throws
}

struct NotificationResponse: Decodable {
    let id: Int
    let content: String
    let isRead: Bool
    let relatedUrl: String?
    let type: String
    let createdAt: String

    enum CodingKeys: String, CodingKey {
        case id, content, relatedUrl, type, createdAt
        case isRead = "read"  // 서버에서 "read"로 반환
    }
}

struct FcmTokenRequest: Encodable {
    let token: String
}

final class NotificationServiceImpl: NotificationService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getNotifications() async throws -> [NotificationResponse] {
        let endpoint = APIEndpoint(path: "/api/notifications", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func markAsRead(id: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/notifications/\(id)/read", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func registerToken(token: String) async throws {
        let endpoint = APIEndpoint(path: "/api/notifications/token", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: FcmTokenRequest(token: token), queryItems: nil)
    }

    func subscribe(token: String) async throws {
        let endpoint = APIEndpoint(path: "/api/notifications/subscribe", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: FcmTokenRequest(token: token), queryItems: nil)
    }

    func unsubscribe(token: String) async throws {
        let endpoint = APIEndpoint(path: "/api/notifications/unsubscribe", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: FcmTokenRequest(token: token), queryItems: nil)
    }
}
