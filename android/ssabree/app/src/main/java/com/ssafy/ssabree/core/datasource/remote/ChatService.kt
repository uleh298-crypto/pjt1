package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageResponse
import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageSendRequest
import com.ssafy.ssabree.core.datasource.remote.model.ChatRoomCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.ChatRoomResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {

    @POST("/api/chat/rooms")
    suspend fun createChatRoom(@Body request: ChatRoomCreateRequest): Long

    @GET("/api/chat/rooms")
    suspend fun getChatRooms(): List<ChatRoomResponse>

    @GET("/api/chat/rooms/{roomId}")
    suspend fun getChatRoom(@Path("roomId") roomId: Long): ChatRoomResponse

    @POST("/api/chat/rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: Long,
        @Body request: ChatMessageSendRequest
    ): Long

    @GET("/api/chat/rooms/{roomId}/messages")
    suspend fun getMessages(@Path("roomId") roomId: Long): List<ChatMessageResponse>

    @DELETE("/api/chat/rooms/{roomId}")
    suspend fun exitChatRoom(@Path("roomId") roomId: Long)
}
