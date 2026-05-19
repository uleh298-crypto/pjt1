package com.ssafy.ssabree.features.board.model

data class PostDetailUiModel(
    val id: Long,
    val boardId: Long,              // 게시판 ID
    val boardName: String,          // "자유게시판"
    val campusName: String,         // "구미 캠퍼스"
    val authorName: String,
    val authorProfileUrl: String?,
    val isAuthor: Boolean,          // 현재 사용자가 작성자인지
    val authorId: Long? = null,     // 쪽지용 작성자 ID (없으면 null)
    val dateText: String,
    val title: String,
    val content: String,
    val imageUrls: List<String>,
    val poll: PollUiModel?,
    val likeCount: Int,
    val isLiked: Boolean,
    val commentCount: Int,
    val bookmarkCount: Int,
    val isBookmarked: Boolean,
    val comments: List<CommentUiModel>,
) {
    companion object {
        fun mock(): PostDetailUiModel {
            return PostDetailUiModel(
                id = 1L,
                boardId = 1L,
                boardName = "자유게시판",
                campusName = "구미 캠퍼스",
                authorName = "싸용자(작성자)",
                authorProfileUrl = null,
                isAuthor = true,
                authorId = 1L,
                dateText = "01/21 12:44",
                title = "구미캠 4층 여자화장실에 폰 놓고 가신 분",
                content = "세면대 옆에 있었어요. 일단 제가 갖고 있겠습니다.\n4층 403호로 와주세요.",
                imageUrls = listOf("https://example.com/phone.jpg"),
                poll = PollUiModel(
                    pollId = 1L,
                    totalVotes = 3,
                    myVotedOptionId = null,
                    options = listOf(
                        PollOptionUiModel(optionId = 1L, text = "짜장면", voteCount = 2, isSelected = false),
                        PollOptionUiModel(optionId = 2L, text = "짬뽕", voteCount = 1, isSelected = false)
                    )
                ),
                likeCount = 120,
                isLiked = false,
                commentCount = 13,
                bookmarkCount = 2,
                isBookmarked = false,
                comments = listOf(
                    CommentUiModel(
                        id = 1L,
                        authorName = "싸용자1",
                        authorProfileUrl = null,
                        isAuthor = false,
                        isMine = false,
                        content = "폰 주인 안에 갇힌거 아님?",
                        dateText = "01/21 12:44",
                        likeCount = 0,
                        isLiked = false,
                        replies = listOf(
                            CommentUiModel(
                                id = 2L,
                                authorName = "싸용자(작성자)",
                                authorProfileUrl = null,
                                isAuthor = true,
                                isMine = true,
                                content = "ㅋㅇㅋㅋ",
                                dateText = "01/21 12:44",
                                likeCount = 0,
                                isLiked = false,
                                replies = emptyList()
                            )
                        )
                    ),
                    CommentUiModel(
                        id = 3L,
                        authorName = "싸용자2",
                        authorProfileUrl = null,
                        isAuthor = false,
                        isMine = false,
                        content = "제거요!!!!!!!! 제거여!!!!!!!!!!!!",
                        dateText = "01/21 12:44",
                        likeCount = 0,
                        isLiked = false,
                        replies = listOf(
                            CommentUiModel(
                                id = 4L,
                                authorName = "싸용자(작성자)",
                                authorProfileUrl = null,
                                isAuthor = true,
                                isMine = true,
                                content = "403호로 오세여 !!!!!!!!!",
                                dateText = "01/21 12:44",
                                likeCount = 0,
                                isLiked = false,
                                replies = emptyList()
                            )
                        )
                    )
                )
            )
        }
    }
}

data class PollUiModel(
    val pollId: Long,
    val totalVotes: Int,
    val myVotedOptionId: Long?,
    val options: List<PollOptionUiModel>
)

data class PollOptionUiModel(
    val optionId: Long,
    val text: String,
    val voteCount: Int,
    val isSelected: Boolean
)
