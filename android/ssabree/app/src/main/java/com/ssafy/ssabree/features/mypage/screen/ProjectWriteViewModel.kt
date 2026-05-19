package com.ssafy.ssabree.features.mypage.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.model.ProjectCreateInfo
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.ProjectUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ProjectWriteVM"

data class ProjectWriteUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val project: ProjectModel? = null,
    val successMessage: String? = null
)

class ProjectWriteViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectWriteUiState())
    val uiState: StateFlow<ProjectWriteUiState> = _uiState.asStateFlow()

    fun loadProject(portfolioId: Long, projectId: Long?) = viewModelScope.launch {
        if (projectId == null) return@launch
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        projectRepository.getProjectsByPortfolio(portfolioId)
            .onSuccess { projects ->
                val project = projects.firstOrNull { it.id == projectId }
                _uiState.update { it.copy(isLoading = false, project = project) }
            }
            .onFailure { e ->
                Log.d(TAG, "loadProject: failed (${e.message})")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    fun saveProject(
        portfolioId: Long,
        projectId: Long?,
        title: String,
        introduction: String,
        description: String,
        techStacks: List<String>,
        urls: List<String>,
        imageUrls: List<String>,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        if (projectId == null) {
            val info = ProjectCreateInfo(
                portfolioId = portfolioId,
                title = title,
                introduction = introduction.ifBlank { null },
                description = description.ifBlank { null },
                techStacks = techStacks,
                urls = urls,
                imageUrls = imageUrls
            )
            projectRepository.createProject(info)
                .onSuccess {
                    Log.d(TAG, "createProject: succeed")
                    _uiState.update { it.copy(isSaving = false, successMessage = "프로젝트가 등록되었습니다.") }
                    onSuccess()
                }
                .onFailure { e ->
                    Log.d(TAG, "createProject: failed (${e.message})")
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
        } else {
            val info = ProjectUpdateInfo(
                title = title,
                introduction = introduction.ifBlank { null },
                description = description.ifBlank { null },
                techStacks = techStacks,
                urls = urls,
                imageUrls = imageUrls
            )
            projectRepository.updateProject(projectId, info)
                .onSuccess {
                    Log.d(TAG, "updateProject: succeed")
                    _uiState.update { it.copy(isSaving = false, successMessage = "프로젝트가 수정되었습니다.") }
                    onSuccess()
                }
                .onFailure { e ->
                    Log.d(TAG, "updateProject: failed (${e.message})")
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
