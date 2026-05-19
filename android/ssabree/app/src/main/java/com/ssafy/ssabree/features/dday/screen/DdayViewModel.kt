package com.ssafy.ssabree.features.dday.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.DdayRepository
import com.ssafy.ssabree.core.repository.model.DdayItemModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DdayViewModel"

data class DdayUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<DdayUiItem> = emptyList()
)

data class DdayUiItem(
    val id: Int,
    val title: String,
    val targetDate: String,
    val dDay: Int,
    val iconKey: String?
)

class DdayViewModel(
    private val repo: DdayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DdayUiState())
    val uiState: StateFlow<DdayUiState> = _uiState.asStateFlow()

    fun loadDdays() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repo.fetchDdays()
            .onSuccess { items ->
                Log.d(TAG, "loadDdays: succeed")
                val uiItems = items.map { item: DdayItemModel ->
                    DdayUiItem(
                        id = item.id,
                        title = item.title,
                        targetDate = item.targetDate,
                        dDay = item.dDay,
                        iconKey = item.iconKey
                    )
                }
                _uiState.update { it.copy(isLoading = false, items = uiItems) }
            }
            .onFailure { e ->
                Log.d(TAG, "loadDdays: failed (${e.message})")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
    }
}
