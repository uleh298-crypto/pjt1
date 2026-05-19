package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskStatusUpdateInfo
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.TaskUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyProgressUiState(
    val tasks: List<TaskUiModel> = emptyList(),
    val selectedTaskId: Long? = null,
    val selectedStatus: String = "IN_PROGRESS",
    val myName: String = "-",
    val myCampus: String? = null,
    val myGeneration: Int? = null,
    val myProfileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isCreated: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class MyProgressViewModel(
    private val repository: MyGroupRepository,
    private val memberRepository: MemberRepository,
    private val groupKind: GroupKind,
    private val groupId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyProgressUiState())
    val uiState: StateFlow<MyProgressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        memberRepository.getMyPage()
            .onSuccess { page ->
                _uiState.update {
                    it.copy(
                        myName = page.user?.name ?: "-",
                        myCampus = page.user?.campus,
                        myGeneration = page.user?.generation,
                        myProfileImageUrl = page.user?.profileImageUrl
                    )
                }
            }
        repository.getTasks(groupKind, groupId)
            .onSuccess { tasks ->
                val mapped = tasks.map { it.toUiModel() }
                _uiState.update {
                    it.copy(
                        tasks = mapped,
                        selectedTaskId = mapped.firstOrNull()?.id,
                        isLoading = false
                    )
                }
            }
            .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
    }

    fun selectTask(taskId: Long) {
        _uiState.update { it.copy(selectedTaskId = taskId) }
    }

    fun selectStatus(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun saveStatus() = viewModelScope.launch {
        val taskId = _uiState.value.selectedTaskId ?: return@launch
        repository.updateTaskStatus(groupKind, taskId, GroupTaskStatusUpdateInfo(status = _uiState.value.selectedStatus))
            .onSuccess { _uiState.update { it.copy(isSaved = true) } }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun createTask(
        title: String,
        content: String,
        startDate: String,
        endDate: String,
        status: String,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isCreating = true, isCreated = false, errorMessage = null) }
        repository.createTask(
            groupKind,
            groupId,
            GroupTaskCreateInfo(
                title = title,
                content = content,
                startDate = startDate,
                endDate = endDate,
                status = status
            )
        ).onSuccess {
            _uiState.update { it.copy(isCreating = false, isCreated = true) }
            load()
            onSuccess()
        }.onFailure { e ->
            _uiState.update { it.copy(isCreating = false, errorMessage = e.message) }
        }
    }

    fun resetSaved() {
        _uiState.update { it.copy(isSaved = false, errorMessage = null) }
    }

    fun resetCreated() {
        _uiState.update { it.copy(isCreated = false, errorMessage = null) }
    }
}
