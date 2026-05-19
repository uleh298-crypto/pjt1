package com.ssafy.ssabree.features.groupapply.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.features.group.model.GroupKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupApplyUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class GroupApplyViewModel(
    private val groupRepository: GroupRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupApplyUiState())
    val uiState: StateFlow<GroupApplyUiState> = _uiState.asStateFlow()

    fun submit(info: GroupApplyInfo) = viewModelScope.launch {
        if (_uiState.value.isSubmitting) return@launch
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        val isMember = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudies().getOrNull()?.any { it.id == groupId } == true
        } else {
            groupRepository.getMyTeams().getOrNull()?.any { it.id == groupId } == true
        }

        if (isMember) {
            _uiState.update { it.copy(isSubmitting = false, errorMessage = "이미 참여 중인 그룹입니다.") }
            return@launch
        }

        val myApplications = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudyApplications().getOrNull().orEmpty()
        } else {
            groupRepository.getMyTeamApplications().getOrNull().orEmpty()
        }

        val hasPending = myApplications.any { app ->
            app.groupId == groupId && app.status == "PENDING"
        }

        if (hasPending) {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = "이미 지원한 그룹입니다."
                )
            }
            return@launch
        }

        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.applyStudy(groupId, info)
        } else {
            groupRepository.applyTeam(groupId, info)
        }
        result.onSuccess {
            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }.onFailure { e ->
            _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(isSuccess = false, errorMessage = null) }
    }
}
