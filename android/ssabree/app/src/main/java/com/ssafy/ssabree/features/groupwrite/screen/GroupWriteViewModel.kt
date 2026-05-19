package com.ssafy.ssabree.features.groupwrite.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.CampusRepository
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupUpdateInfo
import com.ssafy.ssabree.features.group.model.GroupKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupWriteUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val campusId: Long? = null,
    val errorMessage: String? = null
)

class GroupWriteViewModel(
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val campusRepository: CampusRepository,
    private val groupKind: GroupKind
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupWriteUiState())
    val uiState: StateFlow<GroupWriteUiState> = _uiState.asStateFlow()

    init {
        loadCampusId()
    }

    fun submit(info: GroupCreateInfo) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.createStudy(info)
        } else {
            groupRepository.createTeam(info)
        }
        result.onSuccess {
            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }.onFailure { e ->
            _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
        }
    }

    fun update(groupId: Long, info: GroupUpdateInfo) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.updateStudy(groupId, info)
        } else {
            groupRepository.updateTeam(groupId, info)
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

    private fun loadCampusId() = viewModelScope.launch {
        val campusName = memberRepository.getMyPage().getOrNull()?.user?.campus?.trim()
        if (campusName.isNullOrBlank()) {
            _uiState.update { it.copy(campusId = null) }
            return@launch
        }
        val campuses = campusRepository.getCampuses().getOrNull().orEmpty()
        val normalized = normalizeCampusName(campusName)
        val campusId = campuses.firstOrNull { campus ->
            val campusNormalized = normalizeCampusName(campus.name)
            campusNormalized == normalized ||
                campusNormalized.contains(normalized) ||
                normalized.contains(campusNormalized)
        }?.id?.toLong()
        _uiState.update { it.copy(campusId = campusId) }
    }

    private fun normalizeCampusName(name: String): String {
        return name.replace("캠퍼스", "").replace(" ", "").lowercase()
    }
}
