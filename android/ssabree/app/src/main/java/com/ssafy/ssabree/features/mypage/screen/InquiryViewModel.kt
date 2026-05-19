package com.ssafy.ssabree.features.mypage.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.InquiryRepository
import com.ssafy.ssabree.core.repository.model.InquiryModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "InquiryViewModel"

data class InquiryUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val inquiries: List<InquiryModel> = emptyList()
)

class InquiryViewModel(
    private val inquiryRepository: InquiryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InquiryUiState())
    val uiState: StateFlow<InquiryUiState> = _uiState.asStateFlow()

    init {
        loadInquiries()
    }

    fun loadInquiries() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        inquiryRepository.getInquiries()
            .onSuccess { inquiries ->
                Log.d(TAG, "loadInquiries: succeed")
                _uiState.update { it.copy(isLoading = false, inquiries = inquiries) }
            }
            .onFailure { e ->
                Log.d(TAG, "loadInquiries: failed (${e.message})")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    fun submitInquiry(content: String, onSuccess: () -> Unit) = viewModelScope.launch {
        if (content.isBlank()) return@launch
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        inquiryRepository.createInquiry(content)
            .onSuccess {
                Log.d(TAG, "submitInquiry: succeed")
                _uiState.update { it.copy(isSubmitting = false) }
                loadInquiries()
                onSuccess()
            }
            .onFailure { e ->
                Log.d(TAG, "submitInquiry: failed (${e.message})")
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
    }
}
