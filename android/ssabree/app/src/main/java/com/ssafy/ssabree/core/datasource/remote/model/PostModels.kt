package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
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
    @SerializedName(value = "imageUrls", alternate = ["imageUrl", "images", "image_urls"])
    val imageUrls: List<String>?,
    val isBlinded: Boolean = false
)

data class PagedPostResponse(
    val posts: List<PostResponse>,
    val nextCursor: String?,
    val hasNext: Boolean
)

data class PostDetailResponse(
    val createdAt: String?,
    val updatedAt: String?,
    val id: Long,
    val boardId: Long,
    val isMine: Boolean,
    @SerializedName(value = "authorId", alternate = ["memberId"])
    val authorId: Long? = null,
    val title: String,
    val content: String,
    val isBlinded: Boolean,
    @SerializedName(value = "imageUrls", alternate = ["imageUrl", "images", "image_urls"])
    val imageUrls: List<String>?,
    val poll: PollResponse?,
    val likeCount: Int,
    val isLiked: Boolean,
    val commentCount: Int,
    val scrapCount: Int,
    val isScraped: Boolean,
    val comments: List<CommentResponse>
)

data class CommentResponse(
    val id: Long,
    val createdAt: String?,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBlinded: Boolean,
    val anon: AnonResponse?,
    val replies: List<ReplyResponse>
)

data class ReplyResponse(
    val id: Long,
    val createdAt: String?,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBlinded: Boolean,
    val anon: AnonResponse?
)

data class PollResponse(
    val pollId: Long,
    val totalVotes: Int,
    val myVotedOptionId: Long?,
    val options: List<PollOptionResponse>
)

data class PollOptionResponse(
    val optionId: Long,
    val text: String,
    val voteCount: Int
)

data class AnonResponse(
    val name: String,
    val isAuthor: Boolean,
    val isMine: Boolean
)

data class PostLikeResponse(
    val liked: Boolean,
    val likeCount: Int
)

data class ScrapResponse(
    val success: Boolean
)

data class CommentLikeResponse(
    val liked: Boolean,
    val likeCount: Int
)

data class CommentUpdateRequest(
    val content: String
)
