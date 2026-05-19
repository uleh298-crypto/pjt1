package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.CommentLikeResponse
import com.ssafy.ssabree.core.datasource.remote.model.CommentUpdateRequest
import com.ssafy.ssabree.core.datasource.remote.model.CommentResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommentService {
    @PUT("/api/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: CommentUpdateRequest
    ): CommentResponse

    @DELETE("/api/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Long)

    @POST("/api/comments/{commentId}/like")
    suspend fun like(@Path("commentId") commentId: Long): CommentLikeResponse

    @DELETE("/api/comments/{commentId}/like")
    suspend fun unlike(@Path("commentId") commentId: Long): CommentLikeResponse
}
