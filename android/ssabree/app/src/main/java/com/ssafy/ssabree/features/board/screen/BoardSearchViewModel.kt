package com.ssafy.ssabree.features.board.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.KeywordRepository
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.features.board.model.PostUiModel
import com.ssafy.ssabree.features.board.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardSearchUiState(
    val query: String = "",
    val lastSubmittedQuery: String = "",
    val recentKeywords: List<String> = emptyList(),
    val results: List<PostUiModel> = emptyList(),
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val showMinLengthError: Boolean = false,
    val error: String? = null
)

class BoardSearchViewModel(
    private val postRepository: PostRepository,
    private val keywordRepository: KeywordRepository
) : ViewModel() {

    private val pageSize = 20
    private val _uiState = MutableStateFlow(BoardSearchUiState())
    val uiState: StateFlow<BoardSearchUiState> = _uiState.asStateFlow()
    init {
        loadRecentKeywords()
    }

    private fun loadRecentKeywords() {
        val keywords = keywordRepository.getRecentKeywords()
        _uiState.update { it.copy(recentKeywords = keywords) }
    }

    fun onQueryChange(text: String) {
        _uiState.update { state ->
            val trimmed = text.trim()
            if (trimmed == state.lastSubmittedQuery) {
                state.copy(query = text)
            } else {
                state.copy(
                    query = text,
                    results = emptyList(),
                    nextCursor = null,
                    hasNext = false,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    showMinLengthError = false
                )
            }
        }
    }

    fun onSearchSubmit() {
        val keyword = _uiState.value.query.trim()
        if (keyword.length < 2) {
            _uiState.update { it.copy(showMinLengthError = true) }
            return
        }
        keywordRepository.saveKeyword(keyword)
        loadRecentKeywords()
        viewModelScope.launch {
            load(keyword, cursor = null, append = false)
        }
        _uiState.update { it.copy(lastSubmittedQuery = keyword, showMinLengthError = false) }
    }

    fun onQuickSearch(keyword: String) {
        _uiState.update {
            it.copy(
                query = keyword,
                lastSubmittedQuery = keyword,
                results = emptyList(),
                nextCursor = null,
                hasNext = false,
                isLoading = false,
                isLoadingMore = false,
                error = null,
                showMinLengthError = false
            )
        }
    }

    fun deleteRecentKeyword(keyword: String) {
        keywordRepository.deleteKeyword(keyword)
        loadRecentKeywords()
    }

    fun loadMoreIfNeeded(index: Int) {
        val state = _uiState.value
        val shouldLoadMore = state.hasNext &&
                !state.isLoadingMore &&
                !state.isLoading &&
                index >= state.results.size - 3
        if (!shouldLoadMore) return

        viewModelScope.launch {
            val keyword = state.lastSubmittedQuery
            if (keyword.isEmpty()) return@launch
            load(keyword, state.nextCursor, append = true)
        }
    }

    private suspend fun load(keyword: String, cursor: String?, append: Boolean) {
        if (append) {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
        } else {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }

        postRepository.getPosts(keyword = keyword, cursor = cursor, limit = pageSize)
            .onSuccess { page ->
                _uiState.update { state ->
                    val newResults = if (append) {
                        state.results + page.posts.map { it.toUiModel() }
                    } else {
                        page.posts.map { it.toUiModel() }
                    }
                    state.copy(
                        results = newResults,
                        nextCursor = page.nextCursor,
                        hasNext = page.hasNext,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message
                    )
                }
            }
    }
}
