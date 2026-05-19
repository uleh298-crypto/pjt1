package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Common member response used in leader and member lists
 */
data class GroupMemberResponse(
    val id: Long,
    val email: String? = null,
    val name: String?,
    val studentNo: Int? = null,
    val mattermostId: String?,
    val deletedAt: String? = null,
    val profileImageUrl: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Member list item response for /api/studies/{id}/members, /api/teams/{id}/members
 */
data class GroupMemberItemResponse(
    val id: Long,
    val member: GroupMemberResponse,
    val role: String? = null,
    val status: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Study member response for /api/studies/{id}/members
 */
data class StudyMemberResponse(
    val id: Long,
    val studyId: Long,
    val memberId: Long,
    val memberName: String,
    val memberEmail: String,
    val memberProfileImageUrl: String? = null,
    val portfolioId: Long? = null,
    val role: String,
    val status: String,
    val createdAt: String
)

/**
 * Team member response for /api/teams/{id}/members
 */
data class TeamMemberResponse(
    val id: Long,
    val teamId: Long,
    val memberId: Long,
    val memberName: String,
    val memberEmail: String,
    val memberProfileImageUrl: String? = null,
    val portfolioId: Long? = null,
    val role: String,
    val status: String,
    val createdAt: String
)

/**
 * Campus information in group responses
 */
data class CampusResponse(
    val id: Long,
    val name: String
)

/**
 * Group summary response matching backend API
 * Used for list endpoints: GET /api/studies, GET /api/teams, GET /api/studies/me, GET /api/teams/me
 */
data class GroupSummaryResponse(
    val id: Long,
    val leader: GroupMemberResponse?,
    val members: List<GroupMemberResponse>? = null,
    val title: String,
    val campus: CampusResponse? = null,
    val type: String,
    val capacity: Int,
    val description: String? = null,
    val startDate: String,
    val endDate: String,
    val status: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // For backward compatibility
    @SerializedName(
        value = "currentMembers",
        alternate = ["currentMemberCount", "memberCount", "current_members", "member_count", "currentMembersCount"]
    )
    val currentMembers: Int? = null
)

/**
 * Group detail response matching backend API
 * Used for detail endpoints: GET /api/studies/{id}, GET /api/teams/{id}
 */
data class GroupDetailResponse(
    val id: Long,
    val leader: GroupMemberResponse?,
    val title: String,
    val campus: CampusResponse? = null,
    val type: String,
    val capacity: Int,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // For backward compatibility - older API might use 'member' instead of 'leader'
    val member: GroupMemberResponse? = null,
    val members: List<GroupMemberResponse>? = null,
    @SerializedName(
        value = "currentMembers",
        alternate = ["currentMemberCount", "memberCount", "current_members", "member_count", "currentMembersCount"]
    )
    val currentMembers: Int? = null
)

/**
 * Request body for creating a group
 */
data class GroupCreateRequest(
    val title: String,
    val type: String,
    val capacity: Int,
    val startDate: String,
    val endDate: String,
    val campusId: Long,
    val description: String
)

/**
 * Request body for updating a group
 */
data class GroupUpdateRequest(
    val title: String?,
    val type: String?,
    val capacity: Int?,
    val startDate: String?,
    val endDate: String?,
    val campusId: Long?,
    val description: String?,
    val status: String?
)

/**
 * Request body for applying to a group
 */
data class GroupApplicationRequest(
    val portfolioId: Long,
    val title: String,
    val message: String,
    val position: String
)
