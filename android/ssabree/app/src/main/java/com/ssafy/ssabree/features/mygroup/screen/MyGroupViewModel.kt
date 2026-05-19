package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.KeywordRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MyGroupItemUiModel
import com.ssafy.ssabree.features.mygroup.model.toMyGroupUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyGroupUiState(
    val groupKind: GroupKind,
    val selectedFilter: String = "전체",
    val groups: List<MyGroupItemUiModel> = emptyList(),
    val recentKeywords: List<String> = emptyList(),
    val showMinLengthError: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredGroups: List<MyGroupItemUiModel> =
        if (selectedFilter == "전체") {
            groups
        } else {
            groups.filter { it.category == selectedFilter }
        }
}

class MyGroupViewModel(
    private val groupRepository: GroupRepository,
    private val keywordRepository: KeywordRepository,
    private val memberRepository: MemberRepository,
    private val groupKind: GroupKind
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyGroupUiState(groupKind = groupKind))
    val uiState: StateFlow<MyGroupUiState> = _uiState.asStateFlow()

    private val authDataStore = AuthDataStore(ApplicationClass.encryptedSharedPrefManager)

    init {
        load()
        loadRecentKeywords()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudies()
        } else {
            groupRepository.getMyTeams()
        }
        val storedUserId = authDataStore.getUId()?.toLong()
        val currentUserId = storedUserId ?: memberRepository.getMyMemberId().getOrNull()
        android.util.Log.d("MyGroupViewModel", "currentUserId from authDataStore: $currentUserId")
        result.onSuccess { items ->
            val groupUiDeferred = items.map { model ->
                async {
                    val membersResult = if (groupKind == GroupKind.STUDY) {
                        groupRepository.getStudyMembers(model.id)
                    } else {
                        groupRepository.getTeamMembers(model.id)
                    }
                    val members = membersResult.getOrNull().orEmpty()
                    val profileUrls = members.mapNotNull { member -> member.profileImageUrl?.takeIf { it.isNotBlank() } }
                    model.toMyGroupUiModel(groupKind, currentUserId).copy(
                        currentMembers = members.size,
                        memberProfileImageUrls = profileUrls.take(4)
                    )
                }
            }
            _uiState.update {
                it.copy(
                    groups = groupUiDeferred.map { deferred -> deferred.await() },
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
}
