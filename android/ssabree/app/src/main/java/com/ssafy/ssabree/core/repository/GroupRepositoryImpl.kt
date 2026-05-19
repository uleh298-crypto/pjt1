package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.GroupService
import com.ssafy.ssabree.core.datasource.remote.model.toGroupApplicationRequest
import com.ssafy.ssabree.core.datasource.remote.model.toGroupCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toGroupUpdateRequest
import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.core.repository.model.GroupSummaryModel
import com.ssafy.ssabree.core.repository.model.GroupUpdateInfo
import com.ssafy.ssabree.core.repository.model.MyApplicationModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.repository.model.toMemberModel
import com.ssafy.ssabree.core.repository.model.toMyApplicationModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class GroupRepositoryImpl : GroupRepository {
    private val groupService = RetrofitClient.instance.create(GroupService::class.java)

    override suspend fun getStudies(
        campusId: Long?,
        type: String?
    ): Result<List<GroupSummaryModel>> {
        return runCatching {
            groupService.getStudies(campusId = campusId, type = type).map { it.toModel() }
        }
    }

    override suspend fun getTeams(
        campusId: Long?,
        type: String?
    ): Result<List<GroupSummaryModel>> {
        return runCatching {
            groupService.getTeams(campusId = campusId, type = type).map { it.toModel() }
        }
    }

    override suspend fun getStudyDetail(id: Long): Result<GroupDetailModel> {
        return runCatching {
            groupService.getStudyDetail(id).toModel()
        }
    }

    override suspend fun getMyStudies(): Result<List<GroupSummaryModel>> {
        return runCatching {
            groupService.getMyStudies().map { it.toModel() }
        }
    }

    override suspend fun getTeamDetail(id: Long): Result<GroupDetailModel> {
        return runCatching {
            groupService.getTeamDetail(id).toModel()
        }
    }

    override suspend fun getStudyMembers(id: Long): Result<List<GroupMemberModel>> {
        return runCatching {
            groupService.getStudyMembers(id).map { it.toMemberModel() }
        }
    }

    override suspend fun getMyTeams(): Result<List<GroupSummaryModel>> {
        return runCatching {
            groupService.getMyTeams().map { it.toModel() }
        }
    }

    override suspend fun getTeamMembers(id: Long): Result<List<GroupMemberModel>> {
        return runCatching {
            groupService.getTeamMembers(id).map { it.toMemberModel() }
        }
    }

    override suspend fun kickStudyMember(studyId: Long, memberId: Long): Result<Unit> {
        return runCatching {
            groupService.kickStudyMember(studyId, memberId)
        }
    }

    override suspend fun createStudy(info: GroupCreateInfo): Result<GroupSummaryModel> {
        return runCatching {
            groupService.createStudy(info.toGroupCreateRequest()).toModel()
        }
    }

    override suspend fun createTeam(info: GroupCreateInfo): Result<GroupSummaryModel> {
        return runCatching {
            groupService.createTeam(info.toGroupCreateRequest()).toModel()
        }
    }

    override suspend fun updateStudy(id: Long, info: GroupUpdateInfo): Result<Unit> {
        return runCatching {
            groupService.updateStudy(id, info.toGroupUpdateRequest())
        }
    }

    override suspend fun updateTeam(id: Long, info: GroupUpdateInfo): Result<Unit> {
        return runCatching {
            groupService.updateTeam(id, info.toGroupUpdateRequest())
        }
    }

    override suspend fun deleteStudy(id: Long): Result<Unit> {
        return runCatching {
            groupService.deleteStudy(id)
        }
    }

    override suspend fun deleteTeam(id: Long): Result<Unit> {
        return runCatching {
            groupService.deleteTeam(id)
        }
    }

    override suspend fun applyStudy(studyId: Long, info: GroupApplyInfo): Result<Unit> {
        return runCatching {
            groupService.applyStudy(studyId, info.toGroupApplicationRequest())
        }
    }

    override suspend fun applyTeam(teamId: Long, info: GroupApplyInfo): Result<Unit> {
        return runCatching {
            groupService.applyTeam(teamId, info.toGroupApplicationRequest())
        }
    }

    override suspend fun kickTeamMember(teamId: Long, memberId: Long): Result<Unit> {
        return runCatching {
            groupService.kickTeamMember(teamId, memberId)
        }
    }

    override suspend fun getMyStudyApplications(): Result<List<MyApplicationModel>> {
        return runCatching {
            groupService.getMyStudyApplications().map { it.toMyApplicationModel() }
        }
    }

    override suspend fun getMyTeamApplications(): Result<List<MyApplicationModel>> {
        return runCatching {
            groupService.getMyTeamApplications().map { it.toMyApplicationModel() }
        }
    }

    override suspend fun cancelStudyApplication(applicationId: Long): Result<Unit> {
        return runCatching {
            groupService.cancelStudyApplication(applicationId)
        }
    }

    override suspend fun cancelTeamApplication(applicationId: Long): Result<Unit> {
        return runCatching {
            groupService.cancelTeamApplication(applicationId)
        }
    }

    override suspend fun leaveStudy(studyId: Long): Result<Unit> {
        return runCatching {
            groupService.leaveStudy(studyId)
        }
    }

    override suspend fun leaveTeam(teamId: Long): Result<Unit> {
        return runCatching {
            groupService.leaveTeam(teamId)
        }
    }
}
