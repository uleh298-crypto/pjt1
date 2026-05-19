package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.CommentCreateInfo
import com.ssafy.ssabree.core.repository.model.CommentModel
import com.ssafy.ssabree.core.repository.model.PostCreateInfo
import com.ssafy.ssabree.core.repository.model.PostDetailModel
import com.ssafy.ssabree.core.repository.model.PostLikeModel
import com.ssafy.ssabree.core.repository.model.PostModel
import com.ssafy.ssabree.core.repository.model.PagedPostModel
import com.ssafy.ssabree.core.repository.model.PostUpdateInfo
import com.ssafy.ssabree.core.repository.model.PollModel
import com.ssafy.ssabree.core.repository.model.ReplyCreateInfo
import com.ssafy.ssabree.core.repository.model.ReplyModel
import com.ssafy.ssabree.core.repository.model.ScrapModel
import com.ssafy.ssabree.core.repository.model.VoteInfo

interface PostRepository {
    suspend fun getPosts(
        boardId: Long? = null,
        keyword: String? = null,
        cursor: String? = null,
        limit: Int = 20
    ): Result<PagedPostModel>
    suspend fun getHotPosts(
        cursor: String? = null,
        limit: Int = 20
    ): Result<PagedPostModel>
    suspend fun getPostDetail(id: Long): Result<PostDetailModel>
    suspend fun createPost(info: PostCreateInfo): Result<PostModel>
    suspend fun updatePost(id: Long, info: PostUpdateInfo): Result<PostModel>
    suspend fun deletePost(id: Long): Result<Unit>

    suspend fun createComment(postId: Long, info: CommentCreateInfo): Result<CommentModel>
    suspend fun createReply(postId: Long, commentId: Long, info: ReplyCreateInfo): Result<ReplyModel>
    suspend fun vote(postId: Long, info: VoteInfo): Result<PollModel>

    suspend fun like(postId: Long): Result<PostLikeModel>
    suspend fun unlike(postId: Long): Result<PostLikeModel>
    suspend fun scrap(postId: Long): Result<ScrapModel>
    suspend fun unscrap(postId: Long): Result<ScrapModel>
}
