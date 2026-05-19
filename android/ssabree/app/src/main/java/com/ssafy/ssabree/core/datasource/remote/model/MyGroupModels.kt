package com.ssafy.ssabree.core.datasource.remote.model

/**
 * Portfolio information in application response
 */
data class PortfolioSummaryResponse(
    val id: Long,
    val member: GroupMemberResponse? = null,
    // 일부 응답에서 memberName/memberProfileImageUrl이 flat하게 내려오는 경우 대응
    val memberId: Long? = null,
    val memberName: String? = null,
    val memberEmail: String? = null,
    val memberProfileImageUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val introduction: String? = null,
    val bojHandle: String? = null,
    val solvedacRank: String? = null,
    val swTestRank: String? = null,
    val isVisible: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Application response matching backend API
 * GET /api/studies/{id}/applications, GET /api/teams/{id}/applications
 * GET /api/study-applications/{id}, GET /api/team-applications/{id}
 */
data class GroupApplicationResponse(
    val id: Long,
    // Full team/study object from backend
    val team: GroupSummaryResponse? = null,
    val study: GroupSummaryResponse? = null,
    // Full portfolio object from backend
    val portfolio: PortfolioSummaryResponse? = null,
    val title: String,
    val message: String,
    val status: String,
    val position: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // For backward compatibility - older API might use flat fields
    val memberId: Long? = null,
    val portfolioId: Long? = null
)

/**
 * Team application response (updated backend DTO)
 */
data class TeamApplicationResponse(
    val id: Long,
    val team: TeamApplicationTeamInfo,
    val portfolio: TeamApplicationPortfolioInfo,
    val title: String,
    val message: String,
    val position: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TeamApplicationTeamInfo(
    val id: Long,
    val title: String,
    val leaderId: Long,
    val leaderName: String
)

data class TeamApplicationPortfolioInfo(
    val id: Long,
    val title: String,
    val memberId: Long,
    val memberName: String,
    val memberEmail: String,
    val memberProfileImageUrl: String?,
    val introduction: String?,
    val bojHandle: String?,
    val solvedacRank: String?,
    val swTestRank: String?
)

/**
 * Notice response matching backend API
 * GET /api/studies/{id}/notices, GET /api/teams/{id}/notices
 */
data class GroupNoticeResponse(
    val id: Long,
    // New backend response uses teamId/studyId instead of full object
    val teamId: Long? = null,
    val studyId: Long? = null,
    // Backward compatibility for older response
    val team: GroupSummaryResponse? = null,
    val study: GroupSummaryResponse? = null,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String? = null
)

/**
 * Request body for creating/updating a notice
 */
data class GroupNoticeRequest(
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val sendPushNotification: Boolean
)

/**
 * Task response matching backend API
 * GET /api/studies/{id}/tasks, GET /api/teams/{id}/tasks
 */
data class GroupTaskResponse(
    val id: Long,
    // New backend response uses teamId/studyId instead of full object
    val teamId: Long? = null,
    val studyId: Long? = null,
    // Backward compatibility for older response
    val team: GroupSummaryResponse? = null,
    val study: GroupSummaryResponse? = null,
    val title: String,
    val content: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val creatorId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Request body for creating/updating a task
 */
data class GroupTaskRequest(
    val title: String,
    val content: String,
    val startDate: String,
    val endDate: String,
    val status: String
)

/**
 * Request body for updating task status only
 */
data class GroupTaskStatusRequest(
    val status: String
)
