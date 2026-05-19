package com.ssafy.ssabree.features.board.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.core.repository.model.PostUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "BoardEditViewModel"

data class BoardEditUiState(
    val postId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitSuccess: Boolean = false,
    val submitError: String? = null,
) {
    val isSubmitEnabled: Boolean
        get() = title.isNotBlank() &&
            content.isNotBlank() &&
            !isSubmitting
}

class BoardEditViewModel(
    private val postRepository: PostRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardEditUiState())
    val uiState: StateFlow<BoardEditUiState> = _uiState.asStateFlow()

    fun loadPost(postId: Long) {
        _uiState.update { it.copy(postId = postId, isLoading = true) }
        viewModelScope.launch {
            postRepository.getPostDetail(postId)
                .onSuccess { detail ->
                    Log.d(TAG, "loadPost: succeed")
                    _uiState.update {
                        it.copy(
                            postId = postId,
                            title = detail.title,
                            content = detail.content,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.d(TAG, "loadPost: failed (${e.message})")
                    _uiState.update { it.copy(isLoading = false, submitError = e.message) }
                }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value) }
    }

    fun onSubmit() {
        if (!_uiState.value.isSubmitEnabled) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val state = _uiState.value

            postRepository.updatePost(
                state.postId,
                PostUpdateInfo(
                    title = state.title,
                    content = state.content
                )
            ).onFailure { e ->
                Log.d(TAG, "onSubmit: failed (${e.message})")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = e.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
            }.onSuccess {
                Log.d(TAG, "onSubmit: succeed")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSubmitSuccess = true
                    )
                }
            }
        }
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(isSubmitSuccess = false) }
    }
}
