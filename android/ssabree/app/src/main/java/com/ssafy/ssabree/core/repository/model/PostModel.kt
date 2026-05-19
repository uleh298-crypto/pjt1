package com.ssafy.ssabree.core.repository.model

data class PostModel(
    val id: Long,
    val boardId: Long,
    val boardName: String,
    val isMine: Boolean,
    val title: String,
    val content: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val imageUrls: List<String>,
    val isBlinded: Boolean = false
)

data class PagedPostModel(
    val posts: List<PostModel>,
    val nextCursor: String?,
    val hasNext: Boolean
)

data class PostDetailModel(
    val createdAt: String?,
    val updatedAt: String?,
    val id: Long,
    val boardId: Long,
    val isMine: Boolean,
    val authorId: Long? = null,
    val title: String,
    val content: String,
    val isBlinded: Boolean,
    val imageUrls: List<String>,
    val poll: PollModel?,
    val likeCount: Int,
    val isLiked: Boolean,
    val commentCount: Int,
    val scrapCount: Int,
    val isScraped: Boolean,
    val comments: List<CommentModel>
)

data class CommentModel(
    val id: Long,
    val createdAt: String?,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBlinded: Boolean,
    val anon: AnonModel?,
    val replies: List<ReplyModel>
)

data class ReplyModel(
    val id: Long,
    val createdAt: String?,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBlinded: Boolean,
    val anon: AnonModel?
)

data class PollModel(
    val pollId: Long,
    val totalVotes: Int,
    val myVotedOptionId: Long?,
    val options: List<PollOptionModel>
)

data class PollOptionModel(
    val optionId: Long,
    val text: String,
    val voteCount: Int
)

data class AnonModel(
    val name: String,
    val isAuthor: Boolean,
    val isMine: Boolean
)

data class PostLikeModel(
    val liked: Boolean,
    val likeCount: Int
)

data class ScrapModel(
    val success: Boolean
)

data class CommentLikeModel(
    val liked: Boolean,
    val likeCount: Int
)

data class PostCreateInfo(
    val title: String,
    val content: String,
    val boardId: Long,
    val imageUrls: List<String>,
    val poll: PollCreateInfo? = null
)

data class PostUpdateInfo(
    val title: String?,
    val content: String?
)

data class CommentCreateInfo(
    val content: String
)

data class ReplyCreateInfo(
    val content: String
)

data class VoteInfo(
    val optionId: Long
)

data class PollCreateInfo(
    val title: String,
    val options: List<String>
)
