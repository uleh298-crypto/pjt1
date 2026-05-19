import Foundation

// MARK: - Chat Service Protocol

protocol ChatService {
    func createChatRoom(request: ChatRoomCreateRequest) async throws -> Int
    func getChatRooms() async throws -> [ChatRoomResponse]
    func getChatRoom(roomId: Int) async throws -> ChatRoomResponse
    func sendMessage(roomId: Int, request: ChatMessageSendRequest) async throws -> Int
    func getMessages(roomId: Int) async throws -> [ChatMessageResponse]
    func exitChatRoom(roomId: Int) async throws
}

// MARK: - Chat Service Implementation

final class ChatServiceImpl: ChatService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func createChatRoom(request: ChatRoomCreateRequest) async throws -> Int {
        let endpoint = APIEndpoint(path: "/api/chat/rooms", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func getChatRooms() async throws -> [ChatRoomResponse] {
        let endpoint = APIEndpoint(path: "/api/chat/rooms", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getChatRoom(roomId: Int) async throws -> ChatRoomResponse {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func sendMessage(roomId: Int, request: ChatMessageSendRequest) async throws -> Int {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)/messages", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func getMessages(roomId: Int) async throws -> [ChatMessageResponse] {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)/messages", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func exitChatRoom(roomId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}

// MARK: - Legacy MessageService (keeping for backward compatibility during transition)

protocol MessageService {
    func getMessageRooms() async throws -> [ChatRoomResponse]
    func getMessages(roomId: Int) async throws -> [ChatMessageResponse]
    func sendMessage(roomId: Int, content: String) async throws -> Int
}

final class MessageServiceImpl: MessageService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getMessageRooms() async throws -> [ChatRoomResponse] {
        let endpoint = APIEndpoint(path: "/api/chat/rooms", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMessages(roomId: Int) async throws -> [ChatMessageResponse] {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)/messages", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func sendMessage(roomId: Int, content: String) async throws -> Int {
        let endpoint = APIEndpoint(path: "/api/chat/rooms/\(roomId)/messages", method: .POST, requiresAuth: true)
        let request = ChatMessageSendRequest(content: content)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }
}
