package com.ssafy.ssabree.core.datasource.remote.model

data class PostCreateRequest(
    val title: String,
    val content: String,
    val boardId: Long,
    val imageUrls: List<String>,
    val poll: PollCreateRequest?
)

data class PostUpdateRequest(
    val title: String?,
    val content: String?
)

data class CommentCreateRequest(
    val content: String
)

data class ReplyCreateRequest(
    val content: String
)

data class VoteRequest(
    val optionId: Long
)

data class PollCreateRequest(
    val title: String,
    val options: List<String>
)
