package com.ssafy.ssabree.core.repository.model

/**
 * Campus model
 */
data class CampusModel(
    val id: Long,
    val name: String
)

/**
 * Group member model with full details
 */
data class GroupMemberModel(
    val id: Long,
    val email: String? = null,
    val name: String?,
    val studentNo: Int? = null,
    val mattermostId: String?,
    val profileImageUrl: String?,
    val portfolioId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Group summary model for list views
 */
data class GroupSummaryModel(
    val id: Long,
    val title: String,
    val type: String,
    val capacity: Int,
    val startDate: String,
    val endDate: String,
    val description: String? = null,
    val status: String? = null,
    val campus: CampusModel? = null,
    val leader: GroupMemberModel? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // For backward compatibility
    val currentMembers: Int? = null
)

/**
 * Group detail model with full information
 */
data class GroupDetailModel(
    val id: Long,
    val title: String,
    val type: String,
    val capacity: Int,
    val startDate: String,
    val endDate: String,
    val description: String,
    val status: String? = null,
    val campus: CampusModel? = null,
    val leaderId: Long? = null,
    val leaderName: String? = null,
    val leaderEmail: String? = null,
    val leaderMattermostId: String? = null,
    val leaderProfileImageUrl: String? = null,
    val members: List<GroupMemberModel> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val currentMembers: Int? = null
)

/**
 * Input model for creating a group
 */
data class GroupCreateInfo(
    val title: String,
    val type: String,
    val capacity: Int,
    val startDate: String,
    val endDate: String,
    val campusId: Long,
    val description: String
)

/**
 * Input model for updating a group
 */
data class GroupUpdateInfo(
    val title: String? = null,
    val type: String? = null,
    val capacity: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val campusId: Long? = null,
    val description: String? = null,
    val status: String? = null
)

/**
 * Input model for applying to a group
 */
data class GroupApplyInfo(
    val portfolioId: Long,
    val title: String,
    val message: String,
    val position: String
)

/**
 * Portfolio summary model for applications
 */
data class PortfolioSummaryModel(
    val id: Long,
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
    val isVisible: Boolean? = null
)

/**
 * Application model with full details
 */
data class GroupApplicationModel(
    val id: Long,
    val title: String,
    val message: String,
    val position: String,
    val status: String,
    val portfolio: PortfolioSummaryModel? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // For backward compatibility
    val memberId: Long? = null,
    val portfolioId: Long? = null
)

/**
 * My application model for current user
 */
data class MyApplicationModel(
    val id: Long,
    val groupId: Long,
    val groupTitle: String,
    val leaderName: String? = null,
    val status: String,
    val position: String,
    val createdAt: String? = null,
    val isGroupDeleted: Boolean = false
)

/**
 * Notice model
 */
data class GroupNoticeModel(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String? = null
)

/**
 * Input model for creating/updating a notice
 */
data class GroupNoticeCreateInfo(
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val sendPushNotification: Boolean
)

/**
 * Task model
 */
data class GroupTaskModel(
    val id: Long,
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
 * Input model for creating/updating a task
 */
data class GroupTaskCreateInfo(
    val title: String,
    val content: String,
    val startDate: String,
    val endDate: String,
    val status: String
)

/**
 * Input model for updating task status
 */
data class GroupTaskStatusUpdateInfo(
    val status: String
)
