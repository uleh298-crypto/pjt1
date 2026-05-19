package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

// ==================== Request DTOs ====================

data class ChatRoomCreateRequest(
    @SerializedName("postId") val postId: Long?
)

data class ChatMessageSendRequest(
    @SerializedName("content") val content: String
)

data class ChatMessageWebSocketRequest(
    @SerializedName("content") val content: String
)

// ==================== Response DTOs ====================

data class ChatRoomResponse(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("chatRoomName") val chatRoomName: String?,
    @SerializedName("opponentName") val opponentName: String?,
    @SerializedName("postId") val postId: Long?,
    @SerializedName("postTitle") val postTitle: String?,
    @SerializedName("lastMessage") val lastMessage: String?,
    @SerializedName("lastMessageAt") val lastMessageAt: String?,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("createdAt") val createdAt: String?
)

data class ChatMessageResponse(
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("isMine") val isMine: Boolean,
    @SerializedName("senderName") val senderName: String?,
    @SerializedName("content") val content: String,
    @SerializedName("sentAt") val sentAt: String
)

data class ChatListUpdateResponse(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("lastMessage") val lastMessage: String?,
    @SerializedName("lastMessageAt") val lastMessageAt: String?
)

data class ChatRoomCreateResponse(
    @SerializedName("roomId") val roomId: Long
)
