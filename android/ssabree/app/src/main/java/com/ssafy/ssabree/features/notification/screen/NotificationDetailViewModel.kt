package com.ssafy.ssabree.features.notification.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.ssabree.core.datasource.local.NotificationSettingLocalStore
import com.ssafy.ssabree.core.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationSettingUiState(
    val scheduledNotificationEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class NotificationDetailViewModel(
    private val notificationRepository: NotificationRepository,
    private val localStore: NotificationSettingLocalStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingUiState())
    val uiState: StateFlow<NotificationSettingUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(scheduledNotificationEnabled = localStore.isScheduledNotificationEnabled())
        }
    }

    fun updateScheduledNotificationSetting(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                val token = FirebaseMessaging.getInstance().token.await()

                val result = if (enabled) {
                    notificationRepository.subscribeScheduledNotification(token)
                } else {
                    notificationRepository.unsubscribeScheduledNotification(token)
                }

                result.onSuccess {
                    localStore.saveScheduledNotificationEnabled(enabled)
                    _uiState.update { it.copy(scheduledNotificationEnabled = enabled, isSaving = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
