package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.ChatRepository
import com.ssafy.ssabree.core.repository.model.ChatMessageModel
import com.ssafy.ssabree.core.repository.model.ChatRoomCreateInfo
import com.ssafy.ssabree.core.repository.model.ChatRoomModel

class FakeChatRepository : ChatRepository {

    private val fakeRooms = mutableListOf(
        ChatRoomModel(
            roomId = 1,
            chatRoomName = "프로젝트 팀원 모집",
            opponentName = "싸용자123",
            postId = 1L,
            postTitle = "프로젝트 팀원 모집합니다",
            lastMessage = "안녕하세요!",
            lastMessageAt = "2024-01-15T10:30:00",
            isDeleted = false,
            createdAt = "2024-01-14T09:00:00"
        ),
        ChatRoomModel(
            roomId = 2,
            chatRoomName = "스터디 그룹 채팅",
            opponentName = "작성자",
            postId = 2L,
            postTitle = "알고리즘 스터디",
            lastMessage = "내일 만나요",
            lastMessageAt = "2024-01-14T18:00:00",
            isDeleted = false,
            createdAt = "2024-01-13T14:00:00"
        )
    )

    private val fakeMessages = mutableMapOf(
        1L to mutableListOf(
            ChatMessageModel(
                messageId = 1,
                isMine = false,
                senderName = "싸용자123",
                content = "안녕하세요!",
                sentAt = "2024-01-15T10:30:00"
            ),
            ChatMessageModel(
                messageId = 2,
                isMine = true,
                senderName = "나",
                content = "반갑습니다!",
                sentAt = "2024-01-15T10:31:00"
            )
        ),
        2L to mutableListOf(
            ChatMessageModel(
                messageId = 3,
                isMine = false,
                senderName = "작성자",
                content = "스터디 언제 시작할까요?",
                sentAt = "2024-01-14T17:00:00"
            ),
            ChatMessageModel(
                messageId = 4,
                isMine = true,
                senderName = "나",
                content = "내일 오후 어때요?",
                sentAt = "2024-01-14T17:30:00"
            ),
            ChatMessageModel(
                messageId = 5,
                isMine = false,
                senderName = "작성자",
                content = "내일 만나요",
                sentAt = "2024-01-14T18:00:00"
            )
        )
    )

    private var nextRoomId = 3L
    private var nextMessageId = 6L

    override suspend fun createChatRoom(info: ChatRoomCreateInfo): Result<Long> {
        val newRoom = ChatRoomModel(
            roomId = nextRoomId,
            chatRoomName = "새 채팅방",
            opponentName = "싸용자",
            postId = info.postId,
            postTitle = "게시글",
            lastMessage = null,
            lastMessageAt = null,
            isDeleted = false,
            createdAt = "2024-01-15T12:00:00"
        )
        fakeRooms.add(newRoom)
        fakeMessages[nextRoomId] = mutableListOf()
        return Result.success(nextRoomId++)
    }

    override suspend fun getChatRooms(): Result<List<ChatRoomModel>> {
        return Result.success(fakeRooms.filter { !it.isDeleted })
    }

    override suspend fun getChatRoom(roomId: Long): Result<ChatRoomModel> {
        val room = fakeRooms.find { it.roomId == roomId }
        return if (room != null) {
            Result.success(room)
        } else {
            Result.failure(Exception("Chat room not found"))
        }
    }

    override suspend fun getMessages(roomId: Long): Result<List<ChatMessageModel>> {
        val messages = fakeMessages[roomId] ?: emptyList()
        return Result.success(messages)
    }

    override suspend fun sendMessage(roomId: Long, content: String): Result<Long> {
        val messageId = nextMessageId++
        val newMessage = ChatMessageModel(
            messageId = messageId,
            isMine = true,
            senderName = "나",
            content = content,
            sentAt = "2024-01-15T12:00:00"
        )
        fakeMessages.getOrPut(roomId) { mutableListOf() }.add(newMessage)
        return Result.success(messageId)
    }

    override suspend fun exitChatRoom(roomId: Long): Result<Unit> {
        val index = fakeRooms.indexOfFirst { it.roomId == roomId }
        if (index >= 0) {
            fakeRooms[index] = fakeRooms[index].copy(isDeleted = true)
        }
        return Result.success(Unit)
    }

    override suspend fun findRoomByPostId(postId: Long): Result<ChatRoomModel?> {
        val room = fakeRooms.find { it.postId == postId && !it.isDeleted }
        return Result.success(room)
    }
}
