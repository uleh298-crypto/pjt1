package com.ssafy.ssabree.features.groupapply.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PortfolioIdVM"

data class PortfolioIdUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val portfolioId: Long? = null
)

class PortfolioIdViewModel(
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PortfolioIdUiState())
    val uiState: StateFlow<PortfolioIdUiState> = _uiState.asStateFlow()

    init {
        loadPortfolioId()
    }

    fun loadPortfolioId() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        portfolioRepository.getMyPortfolios()
            .onSuccess { portfolios ->
                val portfolioId = portfolios.firstOrNull()?.id
                if (portfolioId == null) {
                    Log.d(TAG, "loadPortfolioId: empty")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "포트폴리오가 없습니다.")
                    }
                } else {
                    Log.d(TAG, "loadPortfolioId: succeed ($portfolioId)")
                    _uiState.update { it.copy(isLoading = false, portfolioId = portfolioId) }
                }
            }
            .onFailure { e ->
                Log.d(TAG, "loadPortfolioId: failed (${e.message})")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }
}
