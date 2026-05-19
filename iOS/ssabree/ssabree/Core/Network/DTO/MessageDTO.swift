import Foundation

// MARK: - Request DTOs

struct ChatRoomCreateRequest: Codable {
    let postId: Int?  // 게시글 ID만 전달, 대상 유저는 백엔드에서 게시글 작성자로 결정
}

struct ChatMessageSendRequest: Codable {
    let content: String
}

struct ChatMessageWebSocketRequest: Codable {
    let content: String
}

// MARK: - Response DTOs

struct ChatRoomResponse: Codable {
    let roomId: Int
    let chatRoomName: String?
    let opponentName: String?
    let postId: Int?
    let postTitle: String?
    let lastMessage: String?
    let lastMessageAt: String?
    let isDeleted: Bool
    let createdAt: String?
}

struct ChatMessageResponse: Codable {
    let messageId: Int
    let isMine: Bool  // 본인 메시지 여부 (senderId 대체)
    let senderName: String?
    let content: String
    let sentAt: String
}

struct ChatListUpdateResponse: Codable {
    let roomId: Int
    let lastMessage: String?
    let lastMessageAt: String?
}

struct ChatRoomCreateResponse: Codable {
    let roomId: Int
}
