package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.ChatService
import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageSendRequest
import com.ssafy.ssabree.core.datasource.remote.model.ChatRoomCreateRequest
import com.ssafy.ssabree.core.repository.model.ChatMessageModel
import com.ssafy.ssabree.core.repository.model.ChatRoomCreateInfo
import com.ssafy.ssabree.core.repository.model.ChatRoomModel
import com.ssafy.ssabree.core.repository.model.toMessageModels
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.repository.model.toRoomModels
import com.ssafy.ssabree.core.utils.RetrofitClient

class ChatRepositoryImpl : ChatRepository {

    private val chatService = RetrofitClient.instance.create(ChatService::class.java)

    override suspend fun createChatRoom(info: ChatRoomCreateInfo): Result<Long> {
        return runCatching {
            val request = ChatRoomCreateRequest(
                postId = info.postId
            )
            chatService.createChatRoom(request)
        }
    }

    override suspend fun getChatRooms(): Result<List<ChatRoomModel>> {
        return runCatching {
            chatService.getChatRooms().toRoomModels()
        }
    }

    override suspend fun getChatRoom(roomId: Long): Result<ChatRoomModel> {
        return runCatching {
            chatService.getChatRoom(roomId).toModel()
        }
    }

    override suspend fun getMessages(roomId: Long): Result<List<ChatMessageModel>> {
        return runCatching {
            chatService.getMessages(roomId).toMessageModels()
        }
    }

    override suspend fun sendMessage(roomId: Long, content: String): Result<Long> {
        return runCatching {
            val request = ChatMessageSendRequest(content = content)
            chatService.sendMessage(roomId, request)
        }
    }

    override suspend fun exitChatRoom(roomId: Long): Result<Unit> {
        return runCatching {
            chatService.exitChatRoom(roomId)
        }
    }

    override suspend fun findRoomByPostId(postId: Long): Result<ChatRoomModel?> {
        return runCatching {
            val rooms = chatService.getChatRooms().toRoomModels()
            rooms.find { it.postId == postId && !it.isDeleted }
        }
    }
}
