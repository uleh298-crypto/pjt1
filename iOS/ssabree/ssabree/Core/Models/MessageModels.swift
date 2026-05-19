import Foundation

// MARK: - Domain Models

struct ChatRoomModel: Identifiable, Equatable {
    let roomId: Int
    let chatRoomName: String
    let opponentName: String
    let postId: Int?
    let postTitle: String?
    let lastMessage: String?
    let lastMessageAt: String?
    let isDeleted: Bool
    let createdAt: String?

    var id: Int { roomId }

    /// 표시할 이름 (빈 문자열이면 "익명의 싸용자")
    var displayName: String {
        chatRoomName.isEmpty ? "익명의 싸용자" : chatRoomName
    }
}

struct ChatMessageModel: Identifiable, Equatable {
    let messageId: Int
    let isMine: Bool  // 본인 메시지 여부 (서버에서 제공)
    let senderName: String
    let content: String
    let sentAt: String

    var id: Int { messageId }
}

struct ChatRoomCreateInfo {
    let postId: Int  // 게시글 ID만 전달, 대상 유저는 백엔드에서 게시글 작성자로 결정
}

struct ChatMessageSendInfo {
    let content: String
}

// MARK: - Extension for Response to Model Conversion

extension ChatRoomResponse {
    func toModel() -> ChatRoomModel {
        ChatRoomModel(
            roomId: roomId,
            chatRoomName: chatRoomName ?? "",
            opponentName: opponentName ?? "익명",
            postId: postId,
            postTitle: postTitle,
            lastMessage: lastMessage,
            lastMessageAt: lastMessageAt,
            isDeleted: isDeleted,
            createdAt: createdAt
        )
    }
}

extension ChatMessageResponse {
    func toModel() -> ChatMessageModel {
        ChatMessageModel(
            messageId: messageId,
            isMine: isMine,
            senderName: senderName ?? "익명",
            content: content,
            sentAt: sentAt
        )
    }
}

extension Array where Element == ChatRoomResponse {
    func toModels() -> [ChatRoomModel] {
        map { $0.toModel() }
    }
}

extension Array where Element == ChatMessageResponse {
    func toModels() -> [ChatMessageModel] {
        map { $0.toModel() }
    }
}
