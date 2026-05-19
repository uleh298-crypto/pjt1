package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.ChatMessageModel
import com.ssafy.ssabree.core.repository.model.ChatRoomCreateInfo
import com.ssafy.ssabree.core.repository.model.ChatRoomModel

interface ChatRepository {
    suspend fun createChatRoom(info: ChatRoomCreateInfo): Result<Long>
    suspend fun getChatRooms(): Result<List<ChatRoomModel>>
    suspend fun getChatRoom(roomId: Long): Result<ChatRoomModel>
    suspend fun getMessages(roomId: Long): Result<List<ChatMessageModel>>
    suspend fun sendMessage(roomId: Long, content: String): Result<Long>
    suspend fun exitChatRoom(roomId: Long): Result<Unit>
    suspend fun findRoomByPostId(postId: Long): Result<ChatRoomModel?>
}
