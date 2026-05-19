package com.ssafy.ssabree.features.message.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.ChatRepository
import com.ssafy.ssabree.core.repository.model.ChatRoomModel
import com.ssafy.ssabree.core.utils.ChatExitTracker
import com.ssafy.ssabree.core.utils.PushEvent
import com.ssafy.ssabree.core.utils.PushEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val TAG = "MessageViewModel"
private const val POLLING_INTERVAL_MS = 30_000L

data class MessageUiState(
    val chatRooms: List<ChatRoomModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExiting: Boolean = false
)

class MessageViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        loadChatRooms()
        observePushEvents()
    }

    private fun observePushEvents() {
        viewModelScope.launch {
            PushEventBus.events.collectLatest { event ->
                when (event) {
                    is PushEvent.OpenChat -> loadChatRooms()
                    else -> Unit
                }
            }
        }
    }

    fun loadChatRooms() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            chatRepository.getChatRooms()
                .onSuccess { rooms ->
                    val sortedRooms = rooms.sortedByDescending { it.lastMessageAt ?: "" }
                    Log.d(TAG, "loadChatRooms: succeed (${rooms.size} rooms)")
                    _uiState.update { it.copy(chatRooms = sortedRooms, isLoading = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "loadChatRooms: failed (${e.message})")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                loadChatRooms()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun exitChatRoom(roomId: Long, postId: Long?) {
        _uiState.update { it.copy(isExiting = true) }
        viewModelScope.launch {
            chatRepository.exitChatRoom(roomId)
                .onSuccess {
                    Log.d(TAG, "exitChatRoom: succeed (roomId=$roomId)")
                    postId?.let { ChatExitTracker.markExited(it) }
                    _uiState.update { state ->
                        state.copy(
                            chatRooms = state.chatRooms.filter { it.roomId != roomId },
                            isExiting = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "exitChatRoom: failed (${e.message})")
                    _uiState.update { it.copy(isExiting = false, error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
