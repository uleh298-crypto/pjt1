package com.ssafy.ssabree.features.groupdetail.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.features.group.model.GroupDetailUiModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val detail: GroupDetailUiModel? = null,
    val isLoading: Boolean = false,
    val isLeader: Boolean = false,
    val isMember: Boolean = false,
    val hasPendingApplication: Boolean = false,
    val errorMessage: String? = null
)

class GroupDetailViewModel(
    private val groupRepository: GroupRepository,
    private val memberRepository: com.ssafy.ssabree.core.repository.MemberRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.getStudyDetail(groupId)
        } else {
            groupRepository.getTeamDetail(groupId)
        }
        val membersResult = if (groupKind == GroupKind.STUDY) {
            groupRepository.getStudyMembers(groupId)
        } else {
            groupRepository.getTeamMembers(groupId)
        }
        val myId = memberRepository.getMyMemberId().getOrNull()
        val myGroups = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudies().getOrNull()
        } else {
            groupRepository.getMyTeams().getOrNull()
        } ?: emptyList()
        val myApplications = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudyApplications().getOrNull()
        } else {
            groupRepository.getMyTeamApplications().getOrNull()
        } ?: emptyList()

        result.onSuccess { detail ->
            val currentMembers = membersResult.getOrNull()?.size ?: detail.currentMembers
            val mergedDetail = if (currentMembers != null) detail.copy(currentMembers = currentMembers) else detail
            val isLeader = myId != null && detail.leaderId != null && detail.leaderId == myId
            val isMember = myGroups.any { it.id == groupId }
            val hasPendingApplication = myApplications.any { app ->
                app.groupId == groupId && app.status == "PENDING"
            }
            _uiState.update {
                it.copy(
                    detail = mergedDetail.toUiModel(groupKind),
                    isLoading = false,
                    isLeader = isLeader,
                    isMember = isMember,
                    hasPendingApplication = hasPendingApplication
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) = viewModelScope.launch {
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.deleteStudy(groupId)
        } else {
            groupRepository.deleteTeam(groupId)
        }
        result.onSuccess { onSuccess() }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
