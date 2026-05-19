package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.NoticeUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnnouncementsUiState(
    val notices: List<NoticeUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleteSuccess: Boolean = false
)

class AnnouncementsViewModel(
    private val repository: MyGroupRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getNotices(groupKind, groupId)
            .onSuccess { notices ->
                // isPinned가 true인 항목을 상단에 배치
                val sortedNotices = notices
                    .map { notice -> notice.toUiModel() }
                    .sortedByDescending { it.isPinned }
                _uiState.update {
                    it.copy(
                        notices = sortedNotices,
                        isLoading = false
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    fun deleteNotice(noticeId: Long) = viewModelScope.launch {
        repository.deleteNotice(groupKind, groupId, noticeId)
            .onSuccess {
                _uiState.update { it.copy(isDeleteSuccess = true) }
            }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun resetDeleteSuccess() {
        _uiState.update { it.copy(isDeleteSuccess = false) }
    }

    fun resetError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
