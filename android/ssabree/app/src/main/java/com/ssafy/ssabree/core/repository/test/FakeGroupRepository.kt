package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.core.repository.model.GroupSummaryModel
import com.ssafy.ssabree.core.repository.model.GroupUpdateInfo
import com.ssafy.ssabree.core.repository.model.MyApplicationModel

class FakeGroupRepository : GroupRepository {
    private val studySamples = mutableListOf(
        GroupSummaryModel(
            id = 1,
            title = "알고리즘 스터디",
            type = "ALGORITHM",
            capacity = 4,
            startDate = "2024-02-01",
            endDate = "2024-03-01",
            currentMembers = 2
        ),
        GroupSummaryModel(
            id = 2,
            title = "CS 핵심 스터디",
            type = "CS",
            capacity = 5,
            startDate = "2024-02-10",
            endDate = "2024-03-20",
            currentMembers = 1
        )
    )

    private val teamSamples = mutableListOf(
        GroupSummaryModel(
            id = 101,
            title = "SSAFY 특화 프로젝트 팀",
            type = "SSAFY",
            capacity = 6,
            startDate = "2024-02-01",
            endDate = "2024-04-01",
            currentMembers = 3
        ),
        GroupSummaryModel(
            id = 102,
            title = "공모전 팀원 모집",
            type = "CONTEST",
            capacity = 4,
            startDate = "2024-02-05",
            endDate = "2024-03-10",
            currentMembers = 2
        )
    )

    private val studyApplicationSamples = mutableListOf(
        MyApplicationModel(
            id = 1001L,
            groupId = 1L,
            groupTitle = "알고리즘 스터디",
            leaderName = "김싸피",
            status = "PENDING",
            position = "BE",
            createdAt = "2024-02-01"
        )
    )

    private val teamApplicationSamples = mutableListOf(
        MyApplicationModel(
            id = 2001L,
            groupId = 101L,
            groupTitle = "SSAFY 특화 프로젝트 팀",
            leaderName = "최싸피",
            status = "APPROVED",
            position = "FE",
            createdAt = "2024-02-05"
        )
    )

    override suspend fun getStudies(
        campusId: Long?,
        type: String?
    ): Result<List<GroupSummaryModel>> {
        return Result.success(
            if (type == null) studySamples else studySamples.filter { it.type == type }
        )
    }

    override suspend fun getTeams(
        campusId: Long?,
        type: String?
    ): Result<List<GroupSummaryModel>> {
        return Result.success(
            if (type == null) teamSamples else teamSamples.filter { it.type == type }
        )
    }

    override suspend fun getStudyDetail(id: Long): Result<GroupDetailModel> {
        val sample = studySamples.firstOrNull { it.id == id }
        return Result.success(
            GroupDetailModel(
                id = sample?.id ?: id,
                title = sample?.title ?: "스터디 모집",
                type = sample?.type ?: "ETC",
                capacity = sample?.capacity ?: 4,
                startDate = sample?.startDate ?: "2024-02-01",
                endDate = sample?.endDate ?: "2024-03-01",
                description = "스터디 상세 설명 샘플입니다.",
                leaderId = 1L,
                leaderName = "김싸피",
                leaderMattermostId = "kim_ssafy",
                members = listOf(
                    GroupMemberModel(id = 1L, name = "김싸피", mattermostId = "kim_ssafy", profileImageUrl = null),
                    GroupMemberModel(id = 2L, name = "이싸피", mattermostId = "lee_ssafy", profileImageUrl = null),
                    GroupMemberModel(id = 3L, name = "박싸피", mattermostId = "park_ssafy", profileImageUrl = null)
                ),
                currentMembers = sample?.currentMembers
            )
        )
    }

    override suspend fun getTeamDetail(id: Long): Result<GroupDetailModel> {
        val sample = teamSamples.firstOrNull { it.id == id }
        return Result.success(
            GroupDetailModel(
                id = sample?.id ?: id,
                title = sample?.title ?: "프로젝트 팀 모집",
                type = sample?.type ?: "FREE",
                capacity = sample?.capacity ?: 4,
                startDate = sample?.startDate ?: "2024-02-01",
                endDate = sample?.endDate ?: "2024-03-01",
                description = "프로젝트 상세 설명 샘플입니다.",
                leaderId = 10L,
                leaderName = "최싸피",
                leaderMattermostId = "choi_ssafy",
                members = listOf(
                    GroupMemberModel(id = 10L, name = "최싸피", mattermostId = "choi_ssafy", profileImageUrl = null),
                    GroupMemberModel(id = 11L, name = "정싸피", mattermostId = "jung_ssafy", profileImageUrl = null)
                ),
                currentMembers = sample?.currentMembers
            )
        )
    }

    override suspend fun getStudyMembers(id: Long): Result<List<GroupMemberModel>> {
        return Result.success(getStudyDetail(id).getOrNull()?.members ?: emptyList())
    }

    override suspend fun getTeamMembers(id: Long): Result<List<GroupMemberModel>> {
        return Result.success(getTeamDetail(id).getOrNull()?.members ?: emptyList())
    }

    override suspend fun kickStudyMember(studyId: Long, memberId: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun createStudy(info: GroupCreateInfo): Result<GroupSummaryModel> {
        val newItem = GroupSummaryModel(
            id = (studySamples.maxOfOrNull { it.id } ?: 0) + 1,
            title = info.title,
            type = info.type,
            capacity = info.capacity,
            startDate = info.startDate,
            endDate = info.endDate
        )
        studySamples.add(newItem)
        return Result.success(newItem)
    }

    override suspend fun createTeam(info: GroupCreateInfo): Result<GroupSummaryModel> {
        val newItem = GroupSummaryModel(
            id = (teamSamples.maxOfOrNull { it.id } ?: 100) + 1,
            title = info.title,
            type = info.type,
            capacity = info.capacity,
            startDate = info.startDate,
            endDate = info.endDate
        )
        teamSamples.add(newItem)
        return Result.success(newItem)
    }

    override suspend fun updateStudy(id: Long, info: GroupUpdateInfo): Result<Unit> {
        studySamples.replaceAll { item ->
            if (item.id == id) {
                item.copy(
                    title = info.title ?: item.title,
                    type = info.type ?: item.type,
                    capacity = info.capacity ?: item.capacity,
                    startDate = info.startDate ?: item.startDate,
                    endDate = info.endDate ?: item.endDate
                )
            } else {
                item
            }
        }
        return Result.success(Unit)
    }

    override suspend fun updateTeam(id: Long, info: GroupUpdateInfo): Result<Unit> {
        teamSamples.replaceAll { item ->
            if (item.id == id) {
                item.copy(
                    title = info.title ?: item.title,
                    type = info.type ?: item.type,
                    capacity = info.capacity ?: item.capacity,
                    startDate = info.startDate ?: item.startDate,
                    endDate = info.endDate ?: item.endDate
                )
            } else {
                item
            }
        }
        return Result.success(Unit)
    }

    override suspend fun deleteStudy(id: Long): Result<Unit> {
        studySamples.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun deleteTeam(id: Long): Result<Unit> {
        teamSamples.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun applyStudy(studyId: Long, info: GroupApplyInfo): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun applyTeam(teamId: Long, info: GroupApplyInfo): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun kickTeamMember(teamId: Long, memberId: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getMyStudies(): Result<List<GroupSummaryModel>> {
        return Result.success(studySamples)
    }

    override suspend fun getMyTeams(): Result<List<GroupSummaryModel>> {
        return Result.success(teamSamples)
    }

    override suspend fun getMyStudyApplications(): Result<List<MyApplicationModel>> {
        return Result.success(studyApplicationSamples)
    }

    override suspend fun getMyTeamApplications(): Result<List<MyApplicationModel>> {
        return Result.success(teamApplicationSamples)
    }

    override suspend fun cancelStudyApplication(applicationId: Long): Result<Unit> {
        studyApplicationSamples.removeAll { it.id == applicationId }
        return Result.success(Unit)
    }

    override suspend fun cancelTeamApplication(applicationId: Long): Result<Unit> {
        teamApplicationSamples.removeAll { it.id == applicationId }
        return Result.success(Unit)
    }

    override suspend fun leaveStudy(studyId: Long): Result<Unit> {
        studySamples.removeAll { it.id == studyId }
        return Result.success(Unit)
    }

    override suspend fun leaveTeam(teamId: Long): Result<Unit> {
        teamSamples.removeAll { it.id == teamId }
        return Result.success(Unit)
    }
}
