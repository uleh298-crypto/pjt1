package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.core.repository.model.GroupSummaryModel
import com.ssafy.ssabree.core.repository.model.GroupUpdateInfo
import com.ssafy.ssabree.core.repository.model.MyApplicationModel

interface GroupRepository {
    suspend fun getStudies(campusId: Long? = null, type: String? = null): Result<List<GroupSummaryModel>>
    suspend fun getTeams(campusId: Long? = null, type: String? = null): Result<List<GroupSummaryModel>>
    suspend fun getMyStudies(): Result<List<GroupSummaryModel>>
    suspend fun getMyTeams(): Result<List<GroupSummaryModel>>
    suspend fun getStudyDetail(id: Long): Result<GroupDetailModel>
    suspend fun getTeamDetail(id: Long): Result<GroupDetailModel>
    suspend fun getStudyMembers(id: Long): Result<List<GroupMemberModel>>
    suspend fun getTeamMembers(id: Long): Result<List<GroupMemberModel>>
    suspend fun kickStudyMember(studyId: Long, memberId: Long): Result<Unit>
    suspend fun createStudy(info: GroupCreateInfo): Result<GroupSummaryModel>
    suspend fun createTeam(info: GroupCreateInfo): Result<GroupSummaryModel>
    suspend fun updateStudy(id: Long, info: GroupUpdateInfo): Result<Unit>
    suspend fun updateTeam(id: Long, info: GroupUpdateInfo): Result<Unit>
    suspend fun deleteStudy(id: Long): Result<Unit>
    suspend fun deleteTeam(id: Long): Result<Unit>
    suspend fun applyStudy(studyId: Long, info: GroupApplyInfo): Result<Unit>
    suspend fun applyTeam(teamId: Long, info: GroupApplyInfo): Result<Unit>
    suspend fun kickTeamMember(teamId: Long, memberId: Long): Result<Unit>
    suspend fun getMyStudyApplications(): Result<List<MyApplicationModel>>
    suspend fun getMyTeamApplications(): Result<List<MyApplicationModel>>
    suspend fun cancelStudyApplication(applicationId: Long): Result<Unit>
    suspend fun cancelTeamApplication(applicationId: Long): Result<Unit>
    suspend fun leaveStudy(studyId: Long): Result<Unit>
    suspend fun leaveTeam(teamId: Long): Result<Unit>
}
