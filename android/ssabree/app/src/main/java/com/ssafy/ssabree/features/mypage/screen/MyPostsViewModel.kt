package com.ssafy.ssabree.features.mypage.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.PostModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPostsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val posts: List<PostModel> = emptyList()
)

class MyPostsViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    init {
        loadMyPosts()
    }

    fun refresh() {
        loadMyPosts()
    }

    private fun loadMyPosts() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        memberRepository.getMyPosts()
            .onSuccess { posts ->
                _uiState.update { it.copy(isLoading = false, posts = posts) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }
}
