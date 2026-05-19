package com.ssafy.ssabree.core.repository.model

import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageResponse
import com.ssafy.ssabree.core.datasource.remote.model.ChatRoomResponse

// ==================== Domain Models ====================

data class ChatRoomModel(
    val roomId: Long,
    val chatRoomName: String,
    val opponentName: String,
    val postId: Long?,
    val postTitle: String?,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val isDeleted: Boolean,
    val createdAt: String?
)

data class ChatMessageModel(
    val messageId: Long,
    val isMine: Boolean,
    val senderName: String,
    val content: String,
    val sentAt: String
)

data class ChatRoomCreateInfo(
    val postId: Long?
)

data class ChatMessageSendInfo(
    val content: String
)

// ==================== Extension Functions ====================

fun ChatRoomResponse.toModel(): ChatRoomModel {
    return ChatRoomModel(
        roomId = roomId,
        chatRoomName = chatRoomName ?: "",
        opponentName = opponentName ?: "익명",
        postId = postId,
        postTitle = postTitle,
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        isDeleted = isDeleted,
        createdAt = createdAt
    )
}

fun ChatMessageResponse.toModel(): ChatMessageModel {
    return ChatMessageModel(
        messageId = messageId,
        isMine = isMine,
        senderName = senderName ?: "익명",
        content = content,
        sentAt = sentAt
    )
}

fun List<ChatRoomResponse>.toRoomModels(): List<ChatRoomModel> = map { it.toModel() }

fun List<ChatMessageResponse>.toMessageModels(): List<ChatMessageModel> = map { it.toModel() }
