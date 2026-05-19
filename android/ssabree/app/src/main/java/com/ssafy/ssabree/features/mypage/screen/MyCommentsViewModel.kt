package com.ssafy.ssabree.features.mypage.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.MyCommentModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyCommentsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val comments: List<MyCommentModel> = emptyList()
)

class MyCommentsViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyCommentsUiState())
    val uiState: StateFlow<MyCommentsUiState> = _uiState.asStateFlow()

    init {
        loadMyComments()
    }

    fun refresh() {
        loadMyComments()
    }

    private fun loadMyComments() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        memberRepository.getMyComments()
            .onSuccess { comments ->
                _uiState.update { it.copy(isLoading = false, comments = comments) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }
}
