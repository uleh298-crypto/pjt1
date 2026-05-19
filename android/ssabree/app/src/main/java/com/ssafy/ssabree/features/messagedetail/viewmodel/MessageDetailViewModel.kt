package com.ssafy.ssabree.features.messagedetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageResponse
import com.ssafy.ssabree.core.repository.ChatRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.ChatRoomCreateInfo
import com.ssafy.ssabree.core.repository.model.ChatRoomModel
import com.ssafy.ssabree.core.utils.ChatExitTracker
import com.ssafy.ssabree.core.utils.ChatRoomPresence
import com.ssafy.ssabree.core.utils.ChatWebSocketManager
import com.ssafy.ssabree.core.utils.WebSocketConnectionState
import com.ssafy.ssabree.core.utils.formatChatTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "MessageDetailViewModel"
private const val MAX_MESSAGE_LENGTH = 255

data class ChatMessageUiModel(
    val id: Long,
    val senderName: String,
    val content: String,
    val time: String,
    val isMe: Boolean
)

data class MessageDetailUiState(
    val roomId: Long = 0,
    val chatRoom: ChatRoomModel? = null,
    val messages: List<ChatMessageUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val messageText: String = "",
    val isSending: Boolean = false,
    val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
    val isNewChatMode: Boolean = false,  // 새 채팅방 생성 모드
    val pendingPostId: Long? = null,     // 생성 대기 중인 게시글 ID
    val isExiting: Boolean = false,
    val exitSuccess: Boolean = false,
    val showInvalidRoomDialog: Boolean = false,
    val showClosedChatDialog: Boolean = false
)

class MessageDetailViewModel(
    private val chatRepository: ChatRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()

    private val webSocketManager = ChatWebSocketManager()

    private var currentMemberId: Long = 0

    init {
        observeWebSocket()
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        viewModelScope.launch {
            webSocketManager.incomingMessages.collect { message ->
                handleIncomingMessage(message)
            }
        }
    }

    private fun handleIncomingMessage(response: ChatMessageResponse) {
        Log.d(TAG, "handleIncomingMessage: messageId=${response.messageId}, senderName=${response.senderName}, content=${response.content}, isMine=${response.isMine}")

        val uiMessage = ChatMessageUiModel(
            id = response.messageId,
            senderName = response.senderName ?: "익명",
            content = response.content,
            time = formatChatTime(response.sentAt),
            isMe = response.isMine
        )

        _uiState.update { state ->
            val existingIds = state.messages.map { it.id }.toSet()
            if (uiMessage.id in existingIds) {
                Log.d(TAG, "handleIncomingMessage: message ${uiMessage.id} already exists, skipping")
                state
            } else {
                Log.d(TAG, "handleIncomingMessage: adding message ${uiMessage.id} to list")
                state.copy(messages = state.messages + uiMessage)
            }
        }
    }

    fun loadChatRoom(roomId: Long) {
        if (roomId <= 0L) {
            _uiState.update { it.copy(showInvalidRoomDialog = true) }
            return
        }
        _uiState.update { it.copy(roomId = roomId, isLoading = true, error = null, isNewChatMode = false) }

        viewModelScope.launch {
            // 먼저 현재 사용자 ID를 가져옴
            memberRepository.getMyMemberId()
                .onSuccess { memberId ->
                    currentMemberId = memberId
                    Log.d(TAG, "loadChatRoom: currentMemberId=$currentMemberId")
                }
                .onFailure { e ->
                    Log.e(TAG, "loadChatRoom: failed to get memberId (${e.message})")
                }

            // 채팅방 정보 로드
            chatRepository.getChatRoom(roomId)
                .onSuccess { room ->
                    Log.d(TAG, "loadChatRoom: succeed, opponentName=${room.opponentName}")
                    _uiState.update {
                        it.copy(
                            chatRoom = room,
                            isLoading = false
                        )
                    }
                    loadMessages(roomId)
                    connectWebSocket(roomId)
                }
                .onFailure { e ->
                    Log.e(TAG, "loadChatRoom: failed (${e.message})")
                    val isInvalidRoom = e.message?.contains("삭제된 채팅방", ignoreCase = true) == true ||
                        e.message?.contains("ChatRoom not found", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = if (isInvalidRoom) null else e.message,
                            showInvalidRoomDialog = isInvalidRoom
                        )
                    }
                }
        }
    }

    fun initNewChatRoom(postId: Long) {
        Log.d(TAG, "initNewChatRoom: postId=$postId")

        if (ChatExitTracker.isExited(postId)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    showClosedChatDialog = true,
                    isNewChatMode = false,
                    pendingPostId = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // 먼저 현재 사용자 ID를 가져옴
            memberRepository.getMyMemberId()
                .onSuccess { memberId ->
                    currentMemberId = memberId
                    Log.d(TAG, "initNewChatRoom: currentMemberId=$currentMemberId")
                }
                .onFailure { e ->
                    Log.e(TAG, "initNewChatRoom: failed to get memberId (${e.message})")
                }

            // 이미 해당 게시글로 참여 중인 채팅방이 있는지 확인
            chatRepository.findRoomByPostId(postId)
                .onSuccess { existingRoom ->
                    if (existingRoom != null) {
                        // 기존 채팅방이 있으면 해당 채팅방으로 로드
                        Log.d(TAG, "initNewChatRoom: found existing room ${existingRoom.roomId}")
                        _uiState.update {
                            it.copy(
                                roomId = existingRoom.roomId,
                                chatRoom = existingRoom,
                                isNewChatMode = false,
                                isLoading = false
                            )
                        }
                        loadMessages(existingRoom.roomId)
                        connectWebSocket(existingRoom.roomId)
                    } else {
                        // 기존 채팅방이 없으면 새 채팅방 생성 모드
                        Log.d(TAG, "initNewChatRoom: no existing room, entering new chat mode")
                        _uiState.update {
                            it.copy(
                                isNewChatMode = true,
                                pendingPostId = postId,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "initNewChatRoom: failed to find room (${e.message})")
                    // 실패해도 새 채팅방 생성 모드로 진입
                    _uiState.update {
                        it.copy(
                            isNewChatMode = true,
                            pendingPostId = postId,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadMessages(roomId: Long) {
        viewModelScope.launch {
            chatRepository.getMessages(roomId)
                .onSuccess { messages ->
                    Log.d(TAG, "loadMessages: succeed (${messages.size} messages), currentMemberId=$currentMemberId")
                    val uiMessages = messages.map { msg ->
                        val isMe = msg.isMine
                        Log.d(TAG, "Message ${msg.messageId}: senderName=${msg.senderName}, isMe=$isMe")
                        ChatMessageUiModel(
                            id = msg.messageId,
                            senderName = msg.senderName,
                            content = msg.content,
                            time = formatChatTime(msg.sentAt),
                            isMe = isMe
                        )
                    }
                    _uiState.update { it.copy(messages = uiMessages) }
                }
                .onFailure { e ->
                    Log.e(TAG, "loadMessages: failed (${e.message})")
                    val isInvalidRoom = e.message?.contains("삭제된 채팅방", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            error = if (isInvalidRoom) null else e.message,
                            showInvalidRoomDialog = isInvalidRoom
                        )
                    }
                }
        }
    }

    private fun connectWebSocket(roomId: Long) {
        if (currentMemberId > 0) {
            webSocketManager.connect(roomId, currentMemberId)
            ChatRoomPresence.enter(roomId)
        }
    }

    fun onMessageTextChange(text: String) {
        val trimmed = if (text.length > MAX_MESSAGE_LENGTH) {
            text.take(MAX_MESSAGE_LENGTH)
        } else {
            text
        }
        _uiState.update { it.copy(messageText = trimmed) }
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty()) return
        if (text.length > MAX_MESSAGE_LENGTH) {
            _uiState.update { it.copy(error = "메시지는 최대 ${MAX_MESSAGE_LENGTH}자까지 입력할 수 있습니다.") }
            return
        }

        // 새 채팅방 생성 모드인 경우
        if (_uiState.value.isNewChatMode) {
            val postId = _uiState.value.pendingPostId ?: return
            _uiState.update { it.copy(isSending = true, messageText = "") }

            viewModelScope.launch {
                // 1. 채팅방 생성
                chatRepository.createChatRoom(ChatRoomCreateInfo(postId))
                    .onSuccess { newRoomId ->
                        Log.d(TAG, "createChatRoom: succeed (roomId=$newRoomId)")
                        _uiState.update {
                            it.copy(
                                roomId = newRoomId,
                                isNewChatMode = false,
                                pendingPostId = null,
                            )
                        }

                        // 2. 채팅방 정보 로드
                        chatRepository.getChatRoom(newRoomId)
                            .onSuccess { room ->
                                Log.d(TAG, "getChatRoom after create: succeed")
                                _uiState.update { it.copy(chatRoom = room) }
                            }

                        // 3. 메시지 전송
                        chatRepository.sendMessage(newRoomId, text)
                            .onSuccess { messageId ->
                                Log.d(TAG, "sendMessage after create: succeed (messageId=$messageId)")
                                _uiState.update { it.copy(isSending = false) }
                                loadMessages(newRoomId)
                                connectWebSocket(newRoomId)
                            }
                            .onFailure { e ->
                                Log.e(TAG, "sendMessage after create: failed (${e.message})")
                                _uiState.update { it.copy(isSending = false, error = e.message) }
                            }
                    }
                    .onFailure { e ->
                        Log.e(TAG, "createChatRoom: failed (${e.message})")
                        _uiState.update { it.copy(isSending = false, error = e.message, messageText = text) }
                    }
            }
            return
        }

        // 기존 채팅방 모드
        val roomId = _uiState.value.roomId
        if (roomId == 0L) return

        _uiState.update { it.copy(isSending = true, messageText = "") }

        if (_uiState.value.connectionState is WebSocketConnectionState.Connected) {
            // WebSocket 연결 시: WebSocket으로 전송, 메시지는 WebSocket 브로드캐스트로 수신
            webSocketManager.sendMessage(roomId, text)
            _uiState.update { it.copy(isSending = false) }
        } else {
            // WebSocket 미연결 시: HTTP로 전송 후 메시지 목록 다시 로드
            viewModelScope.launch {
                chatRepository.sendMessage(roomId, text)
                    .onSuccess { messageId ->
                        Log.d(TAG, "sendMessage via HTTP: succeed (messageId=$messageId)")
                        _uiState.update { it.copy(isSending = false) }
                        // 전송 성공 후 메시지 목록 다시 로드
                        loadMessages(roomId)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "sendMessage: failed (${e.message})")
                        val isInvalidRoom = e.message?.contains("삭제된 채팅방", ignoreCase = true) == true
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                error = if (isInvalidRoom) null else e.message,
                                showInvalidRoomDialog = isInvalidRoom
                            )
                        }
                    }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearInvalidRoomDialog() {
        _uiState.update { it.copy(showInvalidRoomDialog = false) }
    }

    fun clearClosedChatDialog() {
        _uiState.update { it.copy(showClosedChatDialog = false) }
    }

    fun exitChatRoom() {
        val roomId = _uiState.value.roomId
        if (roomId == 0L) return

        _uiState.update { it.copy(isExiting = true) }

        viewModelScope.launch {
            chatRepository.exitChatRoom(roomId)
                .onSuccess {
                    Log.d(TAG, "exitChatRoom: succeed (roomId=$roomId)")
                    _uiState.value.chatRoom?.postId?.let { postId ->
                        ChatExitTracker.markExited(postId)
                    }
                    webSocketManager.disconnect()
                    ChatRoomPresence.exit(roomId)
                    _uiState.update { it.copy(isExiting = false, exitSuccess = true) }
                }
                .onFailure { e ->
                    Log.e(TAG, "exitChatRoom: failed (${e.message})")
                    _uiState.update { it.copy(isExiting = false, error = e.message) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
        ChatRoomPresence.exit(_uiState.value.roomId)
    }
}
