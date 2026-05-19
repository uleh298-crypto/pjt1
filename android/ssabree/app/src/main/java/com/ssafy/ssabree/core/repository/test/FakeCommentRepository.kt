package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.CommentRepository
import com.ssafy.ssabree.core.repository.model.CommentLikeModel

class FakeCommentRepository : CommentRepository {

    override suspend fun updateComment(commentId: Long, content: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deleteComment(commentId: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun like(commentId: Long): Result<CommentLikeModel> {
        return Result.success(CommentLikeModel(liked = true, likeCount = 1))
    }

    override suspend fun unlike(commentId: Long): Result<CommentLikeModel> {
        return Result.success(CommentLikeModel(liked = false, likeCount = 0))
    }
}
