package com.ssafy.ssabree.features.group.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.CampusRepository
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.KeywordRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupListItemUiModel
import com.ssafy.ssabree.features.group.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class GroupUiState(
    val groupKind: GroupKind,
    val selectedFilter: String = "전체",
    val groups: List<GroupListItemUiModel> = emptyList(),
    val recentKeywords: List<String> = emptyList(),
    val campusId: Long? = null,
    val showMinLengthError: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredGroups: List<GroupListItemUiModel> =
        if (selectedFilter == "전체") {
            groups
        } else {
            groups.filter { it.categoryLabel == selectedFilter }
        }
}

class GroupViewModel(
    private val groupRepository: GroupRepository,
    private val keywordRepository: KeywordRepository,
    private val memberRepository: MemberRepository,
    private val campusRepository: CampusRepository,
    private val groupKind: GroupKind
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState(groupKind = groupKind))
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()
    init {
        load()
        loadRecentKeywords()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val campusId = _uiState.value.campusId ?: resolveCampusId().also { resolved ->
            _uiState.update { it.copy(campusId = resolved) }
        }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.getStudies(campusId = campusId)
        } else {
            groupRepository.getTeams(campusId = campusId)
        }
        result.onSuccess { items ->
            val uiItems = coroutineScope {
                items.map { model ->
                    async {
                        val membersResult = if (groupKind == GroupKind.STUDY) {
                            groupRepository.getStudyMembers(model.id)
                        } else {
                            groupRepository.getTeamMembers(model.id)
                        }
                        val currentMembers = membersResult.getOrNull()?.size
                            ?: (model.currentMembers ?: 0)
                        model.toUiModel(groupKind, currentMembers)
                    }
                }.awaitAll()
            }
            _uiState.update {
                it.copy(
                    groups = uiItems,
                    isLoading = false
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    private fun loadRecentKeywords() {
        val keywords = keywordRepository.getRecentKeywords()
        _uiState.update { it.copy(recentKeywords = keywords) }
    }

    fun onSearchSubmit(query: String) {
        val keyword = query.trim()
        if (keyword.length < 2) {
            _uiState.update { it.copy(showMinLengthError = true) }
            return
        }
        keywordRepository.saveKeyword(keyword)
        loadRecentKeywords()
        _uiState.update { it.copy(showMinLengthError = false) }
    }

    fun deleteRecentKeyword(keyword: String) {
        keywordRepository.deleteKeyword(keyword)
        loadRecentKeywords()
    }

    fun clearMinLengthError() {
        _uiState.update { it.copy(showMinLengthError = false) }
    }

    private suspend fun resolveCampusId(): Long? {
        val campusName = memberRepository.getMyPage().getOrNull()?.user?.campus?.trim()
        if (campusName.isNullOrBlank()) return null
        val campuses = campusRepository.getCampuses().getOrNull().orEmpty()
        val normalized = normalizeCampusName(campusName)
        return campuses.firstOrNull { campus ->
            val campusNormalized = normalizeCampusName(campus.name)
            campusNormalized == normalized ||
                campusNormalized.contains(normalized) ||
                normalized.contains(campusNormalized)
        }?.id?.toLong()
    }

    private fun normalizeCampusName(name: String): String {
        return name.replace("캠퍼스", "").replace(" ", "").lowercase()
    }

    private fun isExpired(endDate: String): Boolean {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return runCatching {
            val end = input.parse(endDate) ?: return false
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            end.before(calendar.time)
        }.getOrDefault(false)
    }
}
