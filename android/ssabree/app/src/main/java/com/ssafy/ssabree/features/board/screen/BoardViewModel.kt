@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.ssabree.features.board.screen

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.BoardRepository
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.features.board.model.PostUiModel
import com.ssafy.ssabree.features.board.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "BoardViewModel"

data class BoardFilterOption(
    val id: Long?,
    val label: String
) {
    companion object {
        val All = BoardFilterOption(null, "전체보기")
    }
}

data class BoardUiState(
    val selectedFilter: BoardFilterOption = BoardFilterOption.All,
    val filterMenuExpanded: Boolean = false,
    val posts: List<PostUiModel> = emptyList(),
    val filterOptions: List<BoardFilterOption> = listOf(BoardFilterOption.All),
    val nextCursor: String? = null,
    val hasNext: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val selectedBoardName: String = "게시판",
    val isHotSelected: Boolean = false,
    val noticeContent: String? = null
)

class BoardViewModel(
    private val postRepository: PostRepository,
    private val boardRepository: BoardRepository,
    private val initialBoardId: Long? = null // initialBoardId 추가
) : ViewModel() {

    private val pageSize = 20
    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()
    private var lastHotToggleTime = 0L

    init {
        refreshBoardsAndPosts(initialBoardId)
    }

    fun refreshBoardsAndPosts(boardIdToSelect: Long? = null) = viewModelScope.launch {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

        val boardsResult = boardRepository.getBoards()
        val noticeResult = boardRepository.getNotice()
        val filterOptions = listOf(BoardFilterOption.All) + boardsResult.getOrElse { emptyList() }
            .map { BoardFilterOption(id = it.id, label = it.name) }

        val selected = filterOptions.find { it.id == boardIdToSelect }
            ?: filterOptions.firstOrNull()
            ?: BoardFilterOption.All
        val hotSelected = false

        _uiState.update {
            it.copy(
                filterOptions = filterOptions,
                selectedFilter = selected,
                selectedBoardName = selected.label,
                isHotSelected = hotSelected,
                posts = emptyList(),
                nextCursor = null,
                hasNext = true,
                noticeContent = noticeResult.getOrNull()
            )
        }

        loadPosts(selected.id, cursor = null, append = false)
    }

    private suspend fun loadPosts(boardId: Long?, cursor: String?, append: Boolean) {
        if (append) {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        }

        postRepository.getPosts(boardId = boardId, cursor = cursor, limit = pageSize)
            .onSuccess { page ->
                Log.d(TAG, "loadPosts: succeed (append=$append)")
                _uiState.update { state ->
                    val newPosts = if (append) {
                        state.posts + page.posts.map { post -> post.toUiModel() }
                    } else {
                        page.posts.map { post -> post.toUiModel() }
                    }
                    state.copy(
                        posts = newPosts,
                        nextCursor = page.nextCursor,
                        hasNext = page.hasNext,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { e ->
                Log.d(TAG, "loadPosts: failed (${e.message})")
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = e.message
                    )
                }
            }
    }

    private suspend fun loadHotPosts(cursor: String?, append: Boolean) {
        if (append) {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        }

        postRepository.getHotPosts(cursor = cursor, limit = pageSize)
            .onSuccess { page ->
                Log.d(TAG, "loadHotPosts: succeed (append=$append)")
                _uiState.update { state ->
                    val newPosts = if (append) {
                        state.posts + page.posts.map { post -> post.toUiModel() }
                    } else {
                        page.posts.map { post -> post.toUiModel() }
                    }
                    state.copy(
                        posts = newPosts,
                        nextCursor = page.nextCursor,
                        hasNext = page.hasNext,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { e ->
                Log.d(TAG, "loadHotPosts: failed (${e.message})")
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = e.message
                    )
                }
            }
    }

    fun onRefresh(boardIdToSelect: Long? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val isHotMode = currentState.isHotSelected

            if (isHotMode) {
                // hot 모드에서는 boardId 파라미터를 무시하고 항상 hot API만 호출
                loadHotPosts(cursor = null, append = false)
            } else {
                val targetBoardId = boardIdToSelect ?: currentState.selectedFilter.id
                _uiState.update {
                    it.copy(
                        isHotSelected = false,
                        selectedBoardName = currentState.filterOptions.find { option -> option.id == targetBoardId }?.label
                            ?: it.selectedBoardName
                    )
                }
                loadPosts(boardId = targetBoardId, cursor = null, append = false)
            }
        }
    }

    fun onFilterChipClick() {
        _uiState.update { it.copy(filterMenuExpanded = true) }
    }

    fun onFilterMenuDismiss() {
        _uiState.update { it.copy(filterMenuExpanded = false) }
    }

    fun onFilterSelected(filter: BoardFilterOption) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                selectedBoardName = filter.label,
                isHotSelected = false,
                filterMenuExpanded = false,
                isRefreshing = true,
                posts = emptyList(),
                nextCursor = null,
                hasNext = true
            )
        }
        viewModelScope.launch {
            loadPosts(filter.id, cursor = null, append = false)
        }
    }

    fun onHotSelected() {
        val now = System.currentTimeMillis()
        if (now - lastHotToggleTime < 600) return // debounce to reduce rapid reloads
        lastHotToggleTime = now

        val state = _uiState.value

        // 이미 Hot이면 해제하고 현재 선택된 게시판으로 복귀
        if (state.isHotSelected) {
            _uiState.update {
                it.copy(
                    isHotSelected = false,
                    selectedBoardName = state.selectedFilter.label,
                    posts = emptyList(),
                    nextCursor = null,
                    hasNext = true,
                    isRefreshing = true
                )
            }
            viewModelScope.launch {
                loadPosts(state.selectedFilter.id, cursor = null, append = false)
            }
            return
        }

        _uiState.update {
            it.copy(
                isHotSelected = true,
                selectedBoardName = "Hot 게시글",
                filterMenuExpanded = false,
                posts = emptyList(),
                nextCursor = null,
                hasNext = true,
                isRefreshing = true
            )
        }
        viewModelScope.launch {
            loadHotPosts(cursor = null, append = false)
        }
    }

    fun onListEndReached(index: Int) {
        val state = _uiState.value
        val shouldLoadMore = state.hasNext &&
                !state.isLoadingMore &&
                !state.isRefreshing &&
                index >= state.posts.size - 3

        if (shouldLoadMore) {
            viewModelScope.launch {
                if (state.isHotSelected) {
                    loadHotPosts(cursor = state.nextCursor, append = true)
                } else {
                    loadPosts(state.selectedFilter.id, state.nextCursor, append = true)
                }
            }
        }
    }
}
