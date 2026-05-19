package com.ssafy.ssabree.core.datasource.remote.model

data class MyPageResponse(
    val user: MyPageUserInfoResponse?,
    val counts: MyPageCountsResponse?,
    val portfolioSummary: MyPagePortfolioSummaryResponse?
)

data class MyPageUserInfoResponse(
    val userId: Long,
    val name: String,
    val mattermostId: String,
    val campus: String?,
    val generation: Int?,
    val profileImageUrl: String?
)

data class MyPageCountsResponse(
    val postCount: Long,
    val commentCount: Long,
    val scrapCount: Long
)

data class MyPagePortfolioSummaryResponse(
    val techStack: Map<String, String>?,
    val ssafySwRating: String?,
    val solvedAcRank: String?,
    val solvedAcHandle: String?,
    val solvedAcTierName: String?,
    val solvedAcTierImageUrl: String?,
    val solvedAcSolvedCount: Int?,
    val links: List<String>?,
    val projects: List<String>?
)

data class MyCommentResponse(
    val id: Long,
    val content: String,
    val createdAt: String?,
    val isReply: Boolean,
    val postId: Long,
    val postTitle: String,
    val boardId: Long,
    val boardName: String
)

data class UpdateProfileRequest(
    val profileImageUrl: String?
)

data class UpdateProfileResponse(
    val id: Long,
    val email: String?,
    val name: String?,
    val studentNo: Int?,
    val campus: String?,
    val generation: Int?,
    val classNo: Int?,
    val mattermostId: String?,
    val profileImageUrl: String?,
    val deletedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)
