package com.ssafy.ssabree.core.repository.model

data class MyPageModel(
    val user: MyPageUserModel?,
    val counts: MyPageCountsModel?,
    val portfolioSummary: MyPagePortfolioSummaryModel?
)

data class MyPageUserModel(
    val userId: Long,
    val name: String,
    val mattermostId: String,
    val campus: String?,
    val generation: Int?,
    val profileImageUrl: String?
)

data class MyPageCountsModel(
    val postCount: Long,
    val commentCount: Long,
    val scrapCount: Long
)

data class MyPagePortfolioSummaryModel(
    val techStack: Map<String, String>,
    val ssafySwRating: String?,
    val solvedAcRank: String?,
    val solvedAcHandle: String?,
    val solvedAcTierName: String?,
    val solvedAcTierImageUrl: String?,
    val solvedAcSolvedCount: Int?,
    val links: List<String>,
    val projects: List<String>
)

data class MyCommentModel(
    val id: Long,
    val content: String,
    val createdAt: String?,
    val isReply: Boolean,
    val postId: Long,
    val postTitle: String,
    val boardId: Long,
    val boardName: String
)
