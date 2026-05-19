package com.ssafy.ssabree.features.mypage.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.CachedPortfolioDetail
import com.ssafy.ssabree.core.datasource.local.MyPageLocalStore
import com.ssafy.ssabree.core.repository.PortfolioRepository
import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.model.PortfolioCreateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioImageUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioModel
import com.ssafy.ssabree.core.repository.model.PortfolioStackUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUrlModel
import com.ssafy.ssabree.core.repository.model.PortfolioUrlUpdateInfo
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.SolvedacVerifyInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PortfolioDetailVM"

data class PortfolioDetailUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val portfolio: PortfolioModel? = null,
    val projects: List<ProjectModel> = emptyList(),
    val successMessage: String? = null
)

class PortfolioDetailViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PortfolioDetailUiState())
    val uiState: StateFlow<PortfolioDetailUiState> = _uiState.asStateFlow()
    private val localStore = MyPageLocalStore(ApplicationClass.appContext)

    init {
        localStore.loadPortfolioDetail()?.let { cached ->
            _uiState.update { it.copy(isLoading = false, portfolio = cached.portfolio, projects = cached.projects) }
        }
        loadMyPortfolio()
    }

    fun loadMyPortfolio() = viewModelScope.launch {
        val hasCached = _uiState.value.portfolio != null
        _uiState.update { it.copy(isLoading = !hasCached, errorMessage = null) }
        portfolioRepository.getMyPortfolios()
            .onSuccess { portfolios ->
                val portfolio = portfolios.firstOrNull()
                Log.d(TAG, "loadMyPortfolio: succeed")
                _uiState.update { it.copy(isLoading = false, portfolio = portfolio) }
                portfolio?.let {
                    viewModelScope.launch { loadProjects(it.id) }
                }
                localStore.savePortfolioDetail(CachedPortfolioDetail(portfolio = portfolio, projects = _uiState.value.projects))
            }
            .onFailure { e ->
                Log.d(TAG, "loadMyPortfolio: failed (${e.message})")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    private fun loadProjects(portfolioId: Long) = viewModelScope.launch {
        projectRepository.getProjectsByPortfolio(portfolioId)
            .onSuccess { projects ->
                _uiState.update { it.copy(projects = projects) }
                localStore.savePortfolioDetail(CachedPortfolioDetail(portfolio = _uiState.value.portfolio, projects = projects))
            }
            .onFailure { e ->
                Log.d(TAG, "loadProjects: failed (${e.message})")
            }
    }

    fun refreshProjects(portfolioId: Long) {
        loadProjects(portfolioId)
    }

    fun deleteProject(portfolioId: Long, projectId: Long) = viewModelScope.launch {
        projectRepository.deleteProject(projectId)
            .onSuccess {
                Log.d(TAG, "deleteProject: succeed ($projectId)")
                _uiState.update { it.copy(successMessage = "프로젝트가 삭제되었습니다.") }
                loadProjects(portfolioId)
            }
            .onFailure { e ->
                Log.d(TAG, "deleteProject: failed (${e.message})")
                _uiState.update { it.copy(errorMessage = e.message) }
            }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun savePortfolio(
        title: String,
        description: String,
        introduction: String,
        bojHandle: String?,
        solvedacRank: String?,
        swTestRank: String?,
        isVisible: Boolean,
        stacks: List<PortfolioStackUpdateInfo>,
        urls: List<PortfolioUrlUpdateInfo>,
        images: List<PortfolioImageUpdateInfo>,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        val current = _uiState.value.portfolio
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        if (current == null) {
            val createInfo = PortfolioCreateInfo(
                title = title.ifBlank { "포트폴리오" },
                description = description.ifBlank { "-" },
                introduction = introduction.ifBlank { "-" },
                bojHandle = bojHandle,
                solvedacRank = solvedacRank,
                swTestRank = swTestRank,
                isVisible = isVisible,
                stacks = stacks,
                urls = urls,
                images = images
            )
            portfolioRepository.createPortfolio(createInfo)
                .onSuccess { newId ->
                    Log.d(TAG, "createPortfolio: succeed ($newId)")
                    loadMyPortfolio()
                    _uiState.update { it.copy(isSaving = false, successMessage = "포트폴리오가 등록되었습니다.") }
                    onSuccess()
                }
                .onFailure { e ->
                    Log.d(TAG, "createPortfolio: failed (${e.message})")
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
            return@launch
        }

        val updateInfo = PortfolioUpdateInfo(
            title = title.ifBlank { current.title },
            description = description.ifBlank { current.description },
            introduction = introduction.ifBlank { current.introduction },
            bojHandle = bojHandle,
            solvedacRank = solvedacRank,
            swTestRank = swTestRank ?: current.swTestRank,
            isVisible = isVisible,
            stacks = stacks,
            urls = urls,
            images = images
        )

        portfolioRepository.updatePortfolio(current.id, updateInfo)
            .onSuccess {
                Log.d(TAG, "savePortfolio: succeed")
                val updatedPortfolio = current.copy(
                    title = updateInfo.title,
                    description = updateInfo.description,
                    introduction = updateInfo.introduction,
                    bojHandle = updateInfo.bojHandle,
                    solvedacRank = updateInfo.solvedacRank,
                    swTestRank = updateInfo.swTestRank,
                    isVisible = updateInfo.isVisible
                )
                _uiState.update { it.copy(isSaving = false, portfolio = updatedPortfolio, successMessage = "포트폴리오가 수정되었습니다.") }
                onSuccess()
            }
            .onFailure { e ->
                Log.d(TAG, "savePortfolio: failed (${e.message})")
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
    }

    private fun updateGithubUrl(urls: List<PortfolioUrlModel>, githubUrl: String): List<PortfolioUrlModel> {
        val trimmed = githubUrl.trim()
        if (trimmed.isEmpty()) return urls

        val existingIndex = urls.indexOfFirst { (it.type ?: "").equals("github", ignoreCase = true) }
        return if (existingIndex >= 0) {
            urls.mapIndexed { index, url ->
                if (index == existingIndex) url.copy(url = trimmed) else url
            }
        } else {
            urls + PortfolioUrlModel(
                id = (urls.maxOfOrNull { it.id } ?: 0L) + 1L,
                type = "github",
                url = trimmed
            )
        }
    }

    fun verifySolvedacHandle(
        handle: String,
        onSuccess: (SolvedacVerifyInfo) -> Unit,
        onFailure: (String) -> Unit
    ) = viewModelScope.launch {
        portfolioRepository.verifySolvedac(handle)
            .onSuccess { info -> onSuccess(info) }
            .onFailure { e -> onFailure(e.message ?: "Solved.ac 정보를 불러올 수 없습니다.") }
    }
}
