package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.features.group.model.GroupKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnnouncementEditorUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AnnouncementEditorViewModel(
    private val repository: MyGroupRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementEditorUiState())
    val uiState: StateFlow<AnnouncementEditorUiState> = _uiState.asStateFlow()

    fun createNotice(info: GroupNoticeCreateInfo) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        repository.createNotice(groupKind, groupId, info)
            .onSuccess { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } }
            .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) } }
    }

    fun updateNotice(noticeId: Long, info: GroupNoticeCreateInfo) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        repository.updateNotice(groupKind, groupId, noticeId, info)
            .onSuccess { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } }
            .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) } }
    }

    fun resetResult() {
        _uiState.update { it.copy(isSuccess = false, errorMessage = null) }
    }
}
