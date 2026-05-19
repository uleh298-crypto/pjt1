package com.ssafy.ssabree.features.mypage.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.CachedMyPage
import com.ssafy.ssabree.core.datasource.local.MyPageLocalStore
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.PortfolioRepository
import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.UploadRepository
import com.ssafy.ssabree.core.repository.model.MyPageModel
import com.ssafy.ssabree.core.repository.model.MyPagePortfolioSummaryModel
import com.ssafy.ssabree.core.repository.model.PortfolioModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MyPageViewModel"

data class MyPageUiState(
    val isLoading: Boolean = false,
    val isLoadingPortfolio: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String? = null,
    val myPage: MyPageModel? = null
)

class MyPageViewModel(
    private val memberRepository: MemberRepository,
    private val portfolioRepository: PortfolioRepository,
    private val projectRepository: ProjectRepository,
    private val uploadRepository: UploadRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()
    private val localStore = MyPageLocalStore(ApplicationClass.appContext)

    init {
        localStore.loadMyPage()?.myPage?.let { cached ->
            _uiState.update { it.copy(isLoading = false, myPage = cached) }
        }
        loadMyPage()
    }

    fun loadMyPage() = viewModelScope.launch {
        val hasCached = _uiState.value.myPage != null
        val hasPortfolioCached = _uiState.value.myPage?.portfolioSummary != null
        _uiState.update { it.copy(isLoading = !hasCached, isLoadingPortfolio = !hasPortfolioCached, errorMessage = null) }

        coroutineScope {
            val memberDeferred = async { memberRepository.getMyPage() }
            val portfolioDeferred = async { portfolioRepository.getMyPortfolios() }
            val scrapsDeferred = async { memberRepository.getMyScraps() }

            val memberResult = memberDeferred.await()
            val scrapsResult = scrapsDeferred.await()
            val scrapCountOverride = scrapsResult.getOrNull()?.size?.toLong()
            memberResult
                .onSuccess { myPage ->
                    Log.d(TAG, "loadMyPage: member succeed")
                    val adjustedCounts = myPage.counts?.let { counts ->
                        if (scrapCountOverride != null) {
                            counts.copy(scrapCount = scrapCountOverride)
                        } else {
                            counts
                        }
                    }
                    val adjusted = if (adjustedCounts != myPage.counts) {
                        myPage.copy(counts = adjustedCounts)
                    } else {
                        myPage
                    }
                    _uiState.update { it.copy(isLoading = false, myPage = adjusted) }
                    localStore.saveMyPage(CachedMyPage(adjusted))
                }
                .onFailure { e ->
                    Log.d(TAG, "loadMyPage: member failed (${e.message})")
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }

            val portfolioResult = portfolioDeferred.await()
            val portfolio = portfolioResult.getOrNull()?.firstOrNull()
            val projectTitles = portfolio?.let { current ->
                projectRepository.getProjectsByPortfolio(current.id)
                    .getOrNull()
                    ?.map { project -> project.title }
                    .orEmpty()
            }.orEmpty()
            val summary = portfolio?.toSummary(projectTitles)

            _uiState.update { state ->
                if (summary != null) {
                    val base = state.myPage ?: MyPageModel(user = null, counts = null, portfolioSummary = null)
                    val merged = base.copy(portfolioSummary = summary)
                    localStore.saveMyPage(CachedMyPage(merged))
                    state.copy(isLoadingPortfolio = false, myPage = merged)
                } else {
                    state.copy(isLoadingPortfolio = false)
                }
            }
        }
    }

    fun uploadProfileImage(file: File) = viewModelScope.launch {
        val repo = uploadRepository ?: return@launch
        _uiState.update { it.copy(isUploadingImage = true) }
        repo.uploadImage(file)
            .onSuccess { imageUrl ->
                memberRepository.updateProfileImage(imageUrl)
                    .onSuccess {
                        Log.d(TAG, "uploadProfileImage: succeed")
                        loadMyPage()
                    }
                    .onFailure { e ->
                        Log.e(TAG, "updateProfileImage: failed (${e.message})")
                        _uiState.update { it.copy(isUploadingImage = false, errorMessage = e.message) }
                    }
            }
            .onFailure { e ->
                Log.e(TAG, "uploadProfileImage: failed (${e.message})")
                _uiState.update { it.copy(isUploadingImage = false, errorMessage = e.message) }
            }
        _uiState.update { it.copy(isUploadingImage = false) }
    }

    /**
     * 프로필 이미지 삭제 (빈 값으로 업데이트)
     */
    fun deleteProfileImage() = viewModelScope.launch {
        _uiState.update { it.copy(isUploadingImage = true) }
        memberRepository.updateProfileImage("")
            .onSuccess {
                Log.d(TAG, "deleteProfileImage: succeed")
                loadMyPage()
            }
            .onFailure { e ->
                Log.e(TAG, "deleteProfileImage: failed (${e.message})")
                _uiState.update { it.copy(isUploadingImage = false, errorMessage = e.message) }
            }
        _uiState.update { it.copy(isUploadingImage = false) }
    }
}

private fun PortfolioModel.toSummary(projectTitles: List<String>): MyPagePortfolioSummaryModel {
    val techStack = stacks.associate { stack ->
        stack.stackName to (stack.expertLevel ?: "")
    }
    val links = urls.map { url -> url.url }
    return MyPagePortfolioSummaryModel(
        techStack = techStack,
        ssafySwRating = swTestRank,
        solvedAcRank = solvedacRank,
        solvedAcHandle = bojHandle,
        solvedAcTierName = solvedAcInfo?.tierName,
        solvedAcTierImageUrl = solvedAcInfo?.tierImageUrl,
        solvedAcSolvedCount = solvedAcInfo?.solvedCount,
        links = links,
        projects = projectTitles
    )
}
