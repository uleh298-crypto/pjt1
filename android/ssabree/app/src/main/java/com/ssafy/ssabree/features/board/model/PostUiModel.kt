package com.ssafy.ssabree.features.board.model

data class PostUiModel(
    val id: Long,
    val badge: String?,         // "HOT", "BEST" 같은 뱃지
    val boardName: String,      // "자유", "구미", "서울" ...
    val title: String,
    val preview: String,
    val dateText: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val imageUrl: String?,      // null이면 이미지 없음
    val isBlinded: Boolean = false
) {
    companion object {
        fun mock(
            id: Long = 1L,
            badge: String? = null,
            boardName: String = "자유",
            title: String = "테스트 게시글 제목입니다",
            preview: String = "테스트 게시글 미리보기 내용입니다.",
            dateText: String = "01/21 12:44",
            viewCount: Int = 100,
            likeCount: Int = 0,
            commentCount: Int = 0,
            imageUrl: String? = "https://example.com/sample.jpg",
            isBlinded: Boolean = false
        ): PostUiModel {
            return PostUiModel(
                id = id,
                badge = badge,
                boardName = boardName,
                title = title,
                preview = preview,
                dateText = dateText,
                viewCount = viewCount,
                likeCount = likeCount,
                commentCount = commentCount,
                imageUrl = imageUrl,
                isBlinded = isBlinded
            )
        }
    }
}
