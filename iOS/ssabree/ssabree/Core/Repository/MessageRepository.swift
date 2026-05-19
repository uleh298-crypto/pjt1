import Foundation

// MARK: - Chat Repository Protocol

protocol ChatRepository {
    func createChatRoom(info: ChatRoomCreateInfo) async -> Result<Int, Error>
    func getChatRooms() async -> Result<[ChatRoomModel], Error>
    func getChatRoom(roomId: Int) async -> Result<ChatRoomModel, Error>
    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error>
    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error>
    func exitChatRoom(roomId: Int) async -> Result<Void, Error>
    func findRoomByPostId(postId: Int) async -> Result<ChatRoomModel?, Error>
}

// MARK: - Chat Repository Implementation

final class ChatRepositoryImpl: ChatRepository {
    private let chatService: ChatService

    init(chatService: ChatService) {
        self.chatService = chatService
    }

    func createChatRoom(info: ChatRoomCreateInfo) async -> Result<Int, Error> {
        do {
            let request = ChatRoomCreateRequest(postId: info.postId)
            let roomId = try await chatService.createChatRoom(request: request)
            return .success(roomId)
        } catch {
            return .failure(error)
        }
    }

    func getChatRooms() async -> Result<[ChatRoomModel], Error> {
        do {
            let responses = try await chatService.getChatRooms()
            let models = responses.toModels()
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func getChatRoom(roomId: Int) async -> Result<ChatRoomModel, Error> {
        do {
            let response = try await chatService.getChatRoom(roomId: roomId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error> {
        do {
            let responses = try await chatService.getMessages(roomId: roomId)
            let models = responses.toModels()
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error> {
        do {
            let request = ChatMessageSendRequest(content: content)
            let messageId = try await chatService.sendMessage(roomId: roomId, request: request)
            return .success(messageId)
        } catch {
            return .failure(error)
        }
    }

    func exitChatRoom(roomId: Int) async -> Result<Void, Error> {
        do {
            try await chatService.exitChatRoom(roomId: roomId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func findRoomByPostId(postId: Int) async -> Result<ChatRoomModel?, Error> {
        do {
            let responses = try await chatService.getChatRooms()
            let rooms = responses.toModels()
            let matchingRoom = rooms.first { $0.postId == postId && !$0.isDeleted }
            return .success(matchingRoom)
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Fake Chat Repository (for previews and testing)

final class FakeChatRepository: ChatRepository {
    func createChatRoom(info: ChatRoomCreateInfo) async -> Result<Int, Error> {
        .success(1)
    }

    func getChatRooms() async -> Result<[ChatRoomModel], Error> {
        .success([
            ChatRoomModel(
                roomId: 1,
                chatRoomName: "테스트 사용자",
                opponentName: "테스트 사용자",
                postId: nil,
                postTitle: nil,
                lastMessage: "안녕하세요!",
                lastMessageAt: "2025-02-03T10:30:00",
                isDeleted: false,
                createdAt: "2025-02-03T10:00:00"
            ),
            ChatRoomModel(
                roomId: 2,
                chatRoomName: "",
                opponentName: "익명",
                postId: 1,
                postTitle: "스터디 모집",
                lastMessage: "스터디 참여하고 싶습니다",
                lastMessageAt: "2025-02-03T09:00:00",
                isDeleted: false,
                createdAt: "2025-02-03T08:30:00"
            )
        ])
    }

    func getChatRoom(roomId: Int) async -> Result<ChatRoomModel, Error> {
        .success(ChatRoomModel(
            roomId: roomId,
            chatRoomName: "테스트 사용자",
            opponentName: "테스트 사용자",
            postId: nil,
            postTitle: nil,
            lastMessage: "안녕하세요!",
            lastMessageAt: "2025-02-03T10:30:00",
            isDeleted: false,
            createdAt: "2025-02-03T10:00:00"
        ))
    }

    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error> {
        .success([
            ChatMessageModel(
                messageId: 1,
                isMine: false,
                senderName: "테스트 사용자",
                content: "안녕하세요!",
                sentAt: "2025-02-03T10:00:00"
            ),
            ChatMessageModel(
                messageId: 2,
                isMine: true,
                senderName: "나",
                content: "반갑습니다!",
                sentAt: "2025-02-03T10:05:00"
            ),
            ChatMessageModel(
                messageId: 3,
                isMine: false,
                senderName: "테스트 사용자",
                content: "오늘 날씨가 좋네요",
                sentAt: "2025-02-03T10:10:00"
            )
        ])
    }

    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error> {
        .success(100)
    }

    func exitChatRoom(roomId: Int) async -> Result<Void, Error> {
        .success(())
    }

    func findRoomByPostId(postId: Int) async -> Result<ChatRoomModel?, Error> {
        .success(nil)
    }
}

// MARK: - Legacy Message Repository (for backward compatibility)

protocol MessageRepository {
    func getMessageRooms() async -> Result<[ChatRoomModel], Error>
    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error>
    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error>
}

final class MessageRepositoryImpl: MessageRepository {
    private let messageService: MessageService

    init(messageService: MessageService) {
        self.messageService = messageService
    }

    func getMessageRooms() async -> Result<[ChatRoomModel], Error> {
        do {
            let responses = try await messageService.getMessageRooms()
            let models = responses.toModels()
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error> {
        do {
            let responses = try await messageService.getMessages(roomId: roomId)
            let models = responses.toModels()
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error> {
        do {
            let messageId = try await messageService.sendMessage(roomId: roomId, content: content)
            return .success(messageId)
        } catch {
            return .failure(error)
        }
    }
}

final class FakeMessageRepository: MessageRepository {
    func getMessageRooms() async -> Result<[ChatRoomModel], Error> {
        .success([])
    }

    func getMessages(roomId: Int) async -> Result<[ChatMessageModel], Error> {
        .success([])
    }

    func sendMessage(roomId: Int, content: String) async -> Result<Int, Error> {
        .success(1)
    }
}
