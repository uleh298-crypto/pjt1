package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.ApplicationUiModel
import com.ssafy.ssabree.features.mygroup.model.MemberUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class MemberManageUiState(
    val applications: List<ApplicationUiModel> = emptyList(),
    val members: List<MemberUiModel> = emptyList(),
    val leaderId: Long? = null,
    val capacity: Int = 0,
    val memberCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MemberManageViewModel(
    private val groupRepository: GroupRepository,
    private val repository: MyGroupRepository,
    private val memberRepository: MemberRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemberManageUiState())
    val uiState: StateFlow<MemberManageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val appsDeferred = async { repository.getApplications(groupKind, groupId) }
        val detailDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyDetail(groupId)
            } else {
                groupRepository.getTeamDetail(groupId)
            }
        }
        val membersDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyMembers(groupId)
            } else {
                groupRepository.getTeamMembers(groupId)
            }
        }

        val appsResult = appsDeferred.await()
        val detailResult = detailDeferred.await()
        val membersResult = membersDeferred.await()

        val error = listOf(
            appsResult.exceptionOrNull(),
            detailResult.exceptionOrNull(),
            membersResult.exceptionOrNull()
        ).firstOrNull { it != null }

        if (error != null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            return@launch
        }

        val mergedMembers = mergeLeader(
            membersResult.getOrNull().orEmpty(),
            detailResult.getOrNull()
        ).map { it.toUiModel() }
        val enrichedMembers = enrichMattermostIds(mergedMembers)
        val detail = detailResult.getOrNull()
        val memberCount = maxOf(detail?.currentMembers ?: 0, enrichedMembers.size)
        _uiState.update {
            it.copy(
                applications = appsResult.getOrNull()?.map { it.toUiModel() } ?: emptyList(),
                members = enrichedMembers,
                leaderId = detailResult.getOrNull()?.leaderId,
                capacity = detail?.capacity ?: 0,
                memberCount = memberCount,
                isLoading = false
            )
        }
    }

    fun accept(applicationId: Long) = viewModelScope.launch {
        repository.acceptApplication(groupKind, applicationId)
            .onSuccess { load() }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun reject(applicationId: Long) = viewModelScope.launch {
        repository.rejectApplication(groupKind, applicationId)
            .onSuccess { load() }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun kickMember(memberId: Long) = viewModelScope.launch {
        val result = when (groupKind) {
            GroupKind.PROJECT -> groupRepository.kickTeamMember(groupId, memberId)
            GroupKind.STUDY -> groupRepository.kickStudyMember(groupId, memberId)
        }
        result
            .onSuccess {
                _uiState.update { it.copy(successMessage = "멤버가 추방되었습니다.") }
                load()
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun mergeLeader(
        members: List<GroupMemberModel>,
        detail: GroupDetailModel?
    ): List<GroupMemberModel> {
        val leaderId = detail?.leaderId ?: return members
        if (members.any { it.id == leaderId }) return members
        val leader = GroupMemberModel(
            id = leaderId,
            email = detail.leaderEmail,
            name = detail.leaderName,
            mattermostId = detail.leaderMattermostId,
            profileImageUrl = detail.leaderProfileImageUrl
        )
        return members + leader
    }

    private suspend fun enrichMattermostIds(members: List<MemberUiModel>): List<MemberUiModel> = coroutineScope {
        members.map { member ->
            async {
                if (member.mattermostId.isNotBlank() && member.mattermostId != "-") {
                    member
                } else {
                    val mattermostId = memberRepository.getMember(member.id).getOrNull()?.mattermostId
                    if (!mattermostId.isNullOrBlank()) {
                        member.copy(mattermostId = mattermostId)
                    } else {
                        member
                    }
                }
            }
        }.awaitAll()
    }
}
