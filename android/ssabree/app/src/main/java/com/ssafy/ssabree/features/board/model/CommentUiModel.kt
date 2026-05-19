package com.ssafy.ssabree.features.board.model

data class CommentUiModel(
    val id: Long,
    val authorName: String,
    val authorProfileUrl: String?,
    val isAuthor: Boolean,          // 게시글 작성자인지 여부
    val isMine: Boolean,            // 현재 사용자가 작성한 댓글인지 여부
    val content: String,
    val dateText: String,
    val likeCount: Int,
    val isLiked: Boolean = false,
    val isBlinded: Boolean = false,
    val replies: List<CommentUiModel> = emptyList(),  // 대댓글
) {
    companion object {
        fun mock(
            id: Long = 1L,
            authorName: String = "싸용자1",
            authorProfileUrl: String? = null,
            isAuthor: Boolean = false,
            isMine: Boolean = false,
            content: String = "테스트 댓글입니다.",
            dateText: String = "01/21 12:44",
            likeCount: Int = 0,
            isLiked: Boolean = false,
            isBlinded: Boolean = false,
            replies: List<CommentUiModel> = emptyList(),
        ): CommentUiModel {
            return CommentUiModel(
                id = id,
                authorName = authorName,
                authorProfileUrl = authorProfileUrl,
                isAuthor = isAuthor,
                isMine = isMine,
                content = content,
                dateText = dateText,
                likeCount = likeCount,
                isLiked = isLiked,
                isBlinded = isBlinded,
                replies = replies
            )
        }
    }
}
