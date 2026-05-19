package com.ssafy.ssabree.features.mypage.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.StackRepository
import com.ssafy.ssabree.core.repository.model.StackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "StackViewModel"

data class StackUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stacks: List<StackModel> = emptyList()
)

class StackViewModel(
    private val stackRepository: StackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StackUiState())
    val uiState: StateFlow<StackUiState> = _uiState.asStateFlow()

    init {
        loadStacks()
    }

    fun loadStacks() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        stackRepository.getStacks()
            .onSuccess { stacks ->
                Log.d(TAG, "loadStacks: succeed (${stacks.size})")
                _uiState.update { it.copy(isLoading = false, stacks = stacks) }
            }
            .onFailure { e ->
                Log.d(TAG, "loadStacks: failed (${e.message})")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }
}
