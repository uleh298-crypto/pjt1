package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.PostService
import com.ssafy.ssabree.core.datasource.remote.model.toCommentCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toPostCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toPostUpdateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toReplyCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toVoteRequest
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
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class PostRepositoryImpl : PostRepository {

    private val postService = RetrofitClient.instance.create(PostService::class.java)

    override suspend fun getPosts(
        boardId: Long?,
        keyword: String?,
        cursor: String?,
        limit: Int
    ): Result<PagedPostModel> {
        return runCatching {
            postService.getPosts(
                boardId = boardId,
                keyword = keyword,
                cursor = cursor,
                limit = limit
            ).toModel()
        }
    }

    override suspend fun getHotPosts(
        cursor: String?,
        limit: Int
    ): Result<PagedPostModel> {
        return runCatching {
            postService.getHotPosts(
                cursor = cursor,
                limit = limit
            ).toModel()
        }
    }

    override suspend fun getPostDetail(id: Long): Result<PostDetailModel> {
        return runCatching {
            postService.getPost(id).toModel()
        }
    }

    override suspend fun createPost(info: PostCreateInfo): Result<PostModel> {
        return runCatching {
            postService.createPost(info.toPostCreateRequest()).toModel()
        }
    }

    override suspend fun updatePost(id: Long, info: PostUpdateInfo): Result<PostModel> {
        return runCatching {
            postService.updatePost(id, info.toPostUpdateRequest()).toModel()
        }
    }

    override suspend fun deletePost(id: Long): Result<Unit> {
        return runCatching {
            postService.deletePost(id)
        }
    }

    override suspend fun createComment(
        postId: Long,
        info: CommentCreateInfo
    ): Result<CommentModel> {
        return runCatching {
            postService.createComment(postId, info.toCommentCreateRequest()).toModel()
        }
    }

    override suspend fun createReply(
        postId: Long,
        commentId: Long,
        info: ReplyCreateInfo
    ): Result<ReplyModel> {
        return runCatching {
            postService.createReply(postId, commentId, info.toReplyCreateRequest()).toModel()
        }
    }

    override suspend fun vote(postId: Long, info: VoteInfo): Result<PollModel> {
        return runCatching {
            postService.vote(postId, info.toVoteRequest()).toModel()
        }
    }

    override suspend fun like(postId: Long): Result<PostLikeModel> {
        return runCatching {
            postService.like(postId).toModel()
        }
    }

    override suspend fun unlike(postId: Long): Result<PostLikeModel> {
        return runCatching {
            postService.unlike(postId).toModel()
        }
    }

    override suspend fun scrap(postId: Long): Result<ScrapModel> {
        return runCatching {
            postService.scrap(postId).toModel()
        }
    }

    override suspend fun unscrap(postId: Long): Result<ScrapModel> {
        return runCatching {
            postService.unscrap(postId).toModel()
        }
    }
}
