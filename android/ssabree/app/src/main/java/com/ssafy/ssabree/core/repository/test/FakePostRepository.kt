package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.core.repository.model.AnonModel
import com.ssafy.ssabree.core.repository.model.CommentCreateInfo
import com.ssafy.ssabree.core.repository.model.CommentModel
import com.ssafy.ssabree.core.repository.model.PostCreateInfo
import com.ssafy.ssabree.core.repository.model.PostDetailModel
import com.ssafy.ssabree.core.repository.model.PostLikeModel
import com.ssafy.ssabree.core.repository.model.PostModel
import com.ssafy.ssabree.core.repository.model.PagedPostModel
import com.ssafy.ssabree.core.repository.model.PostUpdateInfo
import com.ssafy.ssabree.core.repository.model.PollModel
import com.ssafy.ssabree.core.repository.model.PollOptionModel
import com.ssafy.ssabree.core.repository.model.ReplyCreateInfo
import com.ssafy.ssabree.core.repository.model.ReplyModel
import com.ssafy.ssabree.core.repository.model.ScrapModel
import com.ssafy.ssabree.core.repository.model.VoteInfo

class FakePostRepository : PostRepository {
    private val samplePosts = listOf(
        PostModel(
            id = 1L,
            boardId = 1L,
            boardName = "Free",
            isMine = false,
            title = "First post",
            content = "This is a sample post content for the board.",
            viewCount = 10,
            likeCount = 3,
            commentCount = 2,
            createdAt = "2026-01-30 09:00",
            updatedAt = "2026-01-30 09:00",
            imageUrls = emptyList()
        ),
        PostModel(
            id = 2L,
            boardId = 2L,
            boardName = "Seoul",
            isMine = false,
            title = "Second post",
            content = "Another sample post content.",
            viewCount = 5,
            likeCount = 1,
            commentCount = 0,
            createdAt = "2026-01-30 10:30",
            updatedAt = "2026-01-30 10:30",
            imageUrls = listOf("https://example.com/sample.jpg")
        )
    )

    override suspend fun getPosts(
        boardId: Long?,
        keyword: String?,
        cursor: String?,
        limit: Int
    ): Result<PagedPostModel> {
        val filtered = samplePosts.filter { post ->
            val boardMatch = boardId == null || post.boardId == boardId
            val keywordMatch = keyword.isNullOrBlank() || post.title.contains(keyword, ignoreCase = true) || post.content.contains(keyword, ignoreCase = true)
            boardMatch && keywordMatch
        }
        val page = filtered.take(limit)
        val hasNext = filtered.size > limit
        val nextCursor = if (hasNext) "cursor_mock" else null
        return runCatching { PagedPostModel(page, nextCursor, hasNext) }
    }

    override suspend fun getHotPosts(
        cursor: String?,
        limit: Int
    ): Result<PagedPostModel> {
        val page = samplePosts.take(limit)
        val hasNext = samplePosts.size > limit
        val nextCursor = if (hasNext) "cursor_hot_mock" else null
        return runCatching { PagedPostModel(page, nextCursor, hasNext) }
    }

    override suspend fun getPostDetail(id: Long): Result<PostDetailModel> {
        val comments = listOf(
            CommentModel(
                id = 100L,
                createdAt = "2026-01-30 10:35",
                content = "Nice post!",
                likeCount = 1,
                isLiked = false,
                isBlinded = false,
                anon = AnonModel(name = "Anon", isAuthor = false, isMine = false),
                replies = listOf(
                    ReplyModel(
                        id = 101L,
                        createdAt = "2026-01-30 10:40",
                        content = "Thanks!",
                        likeCount = 0,
                        isLiked = false,
                        isBlinded = false,
                        anon = AnonModel(name = "Author", isAuthor = true, isMine = true)
                    )
                )
            )
        )

        val detail = PostDetailModel(
            createdAt = "2026-01-30 10:30",
            updatedAt = "2026-01-30 10:30",
            id = id,
            boardId = 2L,
            isMine = false,
            authorId = 11L,
            title = "Second post",
            content = "Another sample post content.",
            isBlinded = false,
            imageUrls = listOf("https://example.com/sample.jpg"),
            poll = PollModel(
                pollId = 1L,
                totalVotes = 3,
                myVotedOptionId = null,
                options = listOf(
                    PollOptionModel(optionId = 1L, text = "Option A", voteCount = 2),
                    PollOptionModel(optionId = 2L, text = "Option B", voteCount = 1)
                )
            ),
            likeCount = 1,
            isLiked = false,
            commentCount = comments.size,
            scrapCount = 0,
            isScraped = false,
            comments = comments
        )

        return runCatching { detail }
    }

    override suspend fun createPost(info: PostCreateInfo): Result<PostModel> {
        val post = PostModel(
            id = 999L,
            boardId = info.boardId,
            boardName = "Board",
            isMine = true,
            title = info.title,
            content = info.content,
            viewCount = 0,
            likeCount = 0,
            commentCount = 0,
            createdAt = "2026-01-30 11:00",
            updatedAt = "2026-01-30 11:00",
            imageUrls = info.imageUrls
        )
        return runCatching { post }
    }

    override suspend fun updatePost(id: Long, info: PostUpdateInfo): Result<PostModel> {
        val post = samplePosts.first().copy(
            id = id,
            title = info.title ?: samplePosts.first().title,
            content = info.content ?: samplePosts.first().content
        )
        return runCatching { post }
    }

    override suspend fun deletePost(id: Long): Result<Unit> {
        return runCatching { }
    }

    override suspend fun createComment(postId: Long, info: CommentCreateInfo): Result<CommentModel> {
        val comment = CommentModel(
            id = 200L,
            createdAt = "2026-01-30 11:05",
            content = info.content,
            likeCount = 0,
            isLiked = false,
            isBlinded = false,
            anon = AnonModel(name = "Me", isAuthor = false, isMine = true),
            replies = emptyList()
        )
        return runCatching { comment }
    }

    override suspend fun createReply(
        postId: Long,
        commentId: Long,
        info: ReplyCreateInfo
    ): Result<ReplyModel> {
        val reply = ReplyModel(
            id = 201L,
            createdAt = "2026-01-30 11:06",
            content = info.content,
            likeCount = 0,
            isLiked = false,
            isBlinded = false,
            anon = AnonModel(name = "Me", isAuthor = false, isMine = true)
        )
        return runCatching { reply }
    }

    override suspend fun vote(postId: Long, info: VoteInfo): Result<PollModel> {
        val poll = PollModel(
            pollId = 1L,
            totalVotes = 4,
            myVotedOptionId = info.optionId,
            options = listOf(
                PollOptionModel(optionId = 1L, text = "Option A", voteCount = 3),
                PollOptionModel(optionId = 2L, text = "Option B", voteCount = 1)
            )
        )
        return runCatching { poll }
    }

    override suspend fun like(postId: Long): Result<PostLikeModel> {
        return runCatching { PostLikeModel(liked = true, likeCount = 2) }
    }

    override suspend fun unlike(postId: Long): Result<PostLikeModel> {
        return runCatching { PostLikeModel(liked = false, likeCount = 1) }
    }

    override suspend fun scrap(postId: Long): Result<ScrapModel> {
        return runCatching { ScrapModel(success = true) }
    }

    override suspend fun unscrap(postId: Long): Result<ScrapModel> {
        return runCatching { ScrapModel(success = true) }
    }
}
