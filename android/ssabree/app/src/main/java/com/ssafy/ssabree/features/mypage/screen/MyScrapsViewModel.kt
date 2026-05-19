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

data class MyScrapsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val scraps: List<PostModel> = emptyList()
)

class MyScrapsViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyScrapsUiState())
    val uiState: StateFlow<MyScrapsUiState> = _uiState.asStateFlow()

    init {
        loadMyScraps()
    }

    fun refresh() {
        loadMyScraps()
    }

    private fun loadMyScraps() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        memberRepository.getMyScraps()
            .onSuccess { scraps ->
                _uiState.update { it.copy(isLoading = false, scraps = scraps) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }
}
