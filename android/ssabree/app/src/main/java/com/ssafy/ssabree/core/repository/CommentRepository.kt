package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.CommentLikeModel

interface CommentRepository {
    suspend fun updateComment(commentId: Long, content: String): Result<Unit>
    suspend fun deleteComment(commentId: Long): Result<Unit>
    suspend fun like(commentId: Long): Result<CommentLikeModel>
    suspend fun unlike(commentId: Long): Result<CommentLikeModel>
}
