package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.CommentService
import com.ssafy.ssabree.core.datasource.remote.model.CommentUpdateRequest
import com.ssafy.ssabree.core.repository.model.CommentLikeModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class CommentRepositoryImpl : CommentRepository {

    private val commentService = RetrofitClient.instance.create(CommentService::class.java)

    override suspend fun updateComment(commentId: Long, content: String): Result<Unit> {
        return runCatching {
            commentService.updateComment(commentId, CommentUpdateRequest(content))
        }
    }

    override suspend fun deleteComment(commentId: Long): Result<Unit> {
        return runCatching {
            commentService.deleteComment(commentId)
        }
    }

    override suspend fun like(commentId: Long): Result<CommentLikeModel> {
        return runCatching {
            commentService.like(commentId).toModel()
        }
    }

    override suspend fun unlike(commentId: Long): Result<CommentLikeModel> {
        return runCatching {
            commentService.unlike(commentId).toModel()
        }
    }
}
