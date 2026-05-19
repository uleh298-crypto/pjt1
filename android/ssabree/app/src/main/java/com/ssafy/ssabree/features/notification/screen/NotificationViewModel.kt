package com.ssafy.ssabree.features.notification.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.NotificationRepository
import com.ssafy.ssabree.core.repository.model.NotificationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationModel> = emptyList(),
    val error: String? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            notificationRepository.getNotifications()
                .onSuccess { notifications ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = notifications
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        notifications = _uiState.value.notifications.map { notification ->
                            if (notification.id == id) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                    )
                }
        }
    }
}
