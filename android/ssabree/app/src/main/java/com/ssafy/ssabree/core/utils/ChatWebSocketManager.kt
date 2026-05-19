package com.ssafy.ssabree.core.utils

import android.util.Log
import com.google.gson.Gson
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.datasource.remote.model.ChatListUpdateResponse
import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageResponse
import com.ssafy.ssabree.core.datasource.remote.model.ChatMessageWebSocketRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private const val TAG = "ChatWebSocketManager"

sealed class WebSocketConnectionState {
    data object Disconnected : WebSocketConnectionState()
    data object Connecting : WebSocketConnectionState()
    data object Connected : WebSocketConnectionState()
    data class Error(val message: String) : WebSocketConnectionState()
}

class ChatWebSocketManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val authDataStore by lazy {
        AuthDataStore(ApplicationClass.encryptedSharedPrefManager)
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentRoomId: Long? = null
    private var currentMemberId: Long? = null

    private var stompConnected = false
    private val pendingSubscriptions = mutableListOf<() -> Unit>()

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ChatMessageResponse>(replay = 1, extraBufferCapacity = 10)
    val incomingMessages: SharedFlow<ChatMessageResponse> = _incomingMessages.asSharedFlow()

    private val _chatListUpdates = MutableSharedFlow<ChatListUpdateResponse>(replay = 1, extraBufferCapacity = 10)
    val chatListUpdates: SharedFlow<ChatListUpdateResponse> = _chatListUpdates.asSharedFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            sendConnectFrame()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received: $text")
            handleStompMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code $reason")
            stompConnected = false
            _connectionState.value = WebSocketConnectionState.Disconnected
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
            stompConnected = false
            _connectionState.value = WebSocketConnectionState.Error(t.message ?: "Unknown error")
        }
    }

    fun connect(roomId: Long, memberId: Long) {
        if (_connectionState.value is WebSocketConnectionState.Connected &&
            currentRoomId == roomId && currentMemberId == memberId) {
            Log.d(TAG, "Already connected to room $roomId")
            return
        }

        disconnect()

        currentRoomId = roomId
        currentMemberId = memberId
        _connectionState.value = WebSocketConnectionState.Connecting

        val token = authDataStore.getAccessToken()
        val wsUrl = buildWsUrl()

        Log.d(TAG, "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    private fun buildWsUrl(): String {
        val baseUrl = RetrofitClient.SERVER_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')
        return "$baseUrl/ws-stomp"
    }

    private fun sendConnectFrame() {
        val token = authDataStore.getAccessToken() ?: ""
        val connectFrame = buildString {
            append("CONNECT\n")
            append("accept-version:1.1,1.2\n")
            append("heart-beat:10000,10000\n")
            if (token.isNotBlank()) {
                append("Authorization:Bearer $token\n")
            }
            append("\n")
            append("\u0000")
        }
        webSocket?.send(connectFrame)
        Log.d(TAG, "Sent CONNECT frame")
    }

    private fun handleStompMessage(message: String) {
        when {
            message.startsWith("CONNECTED") -> {
                Log.d(TAG, "STOMP CONNECTED")
                stompConnected = true
                _connectionState.value = WebSocketConnectionState.Connected

                val roomId = currentRoomId
                val memberId = currentMemberId
                if (roomId != null && memberId != null) {
                    subscribeToRoom(roomId, memberId)
                    subscribeToChatList(memberId)
                }

                pendingSubscriptions.forEach { it.invoke() }
                pendingSubscriptions.clear()
            }
            message.startsWith("MESSAGE") -> {
                handleMessageFrame(message)
            }
            message.startsWith("ERROR") -> {
                Log.e(TAG, "STOMP ERROR: $message")
                val errorBody = extractBody(message)
                _connectionState.value = WebSocketConnectionState.Error(errorBody)
            }
            message == "\n" || message == "\r\n" -> {
                // Heartbeat - 응답 보내기
                webSocket?.send("\n")
            }
        }
    }

    private fun handleMessageFrame(frame: String) {
        val destination = extractHeader(frame, "destination")
        val body = extractBody(frame)

        Log.d(TAG, "Message from $destination: $body")

        scope.launch {
            try {
                when {
                    destination?.contains("/topic/user/") == true &&
                    destination.contains("/chat/") &&
                    !destination.contains("chat-list") -> {
                        val messageResponse = gson.fromJson(body, ChatMessageResponse::class.java)
                        _incomingMessages.emit(messageResponse)
                    }
                    destination?.contains("/chat-list") == true -> {
                        val updateResponse = gson.fromJson(body, ChatListUpdateResponse::class.java)
                        _chatListUpdates.emit(updateResponse)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message: ${e.message}")
            }
        }
    }

    private fun extractHeader(frame: String, headerName: String): String? {
        val lines = frame.split("\n")
        for (line in lines) {
            if (line.startsWith("$headerName:")) {
                return line.substringAfter("$headerName:").trim()
            }
        }
        return null
    }

    private fun extractBody(frame: String): String {
        val parts = frame.split("\n\n", limit = 2)
        return if (parts.size > 1) {
            parts[1].replace("\u0000", "").trim()
        } else {
            ""
        }
    }

    private fun subscribeToRoom(roomId: Long, memberId: Long) {
        val subscribeAction: () -> Unit = {
            val subscribeFrame = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-room-$memberId-$roomId\n")
                append("destination:/topic/user/$memberId/chat/$roomId\n")
                append("\n")
                append("\u0000")
            }
            webSocket?.send(subscribeFrame)
            Log.d(TAG, "Subscribed to /topic/user/$memberId/chat/$roomId")
        }

        if (stompConnected) {
            subscribeAction()
        } else {
            pendingSubscriptions.add(subscribeAction)
        }
    }

    private fun subscribeToChatList(memberId: Long) {
        val subscribeAction: () -> Unit = {
            val subscribeFrame = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-chatlist-$memberId\n")
                append("destination:/topic/user/$memberId/chat-list\n")
                append("\n")
                append("\u0000")
            }
            webSocket?.send(subscribeFrame)
            Log.d(TAG, "Subscribed to /topic/user/$memberId/chat-list")
        }

        if (stompConnected) {
            subscribeAction()
        } else {
            pendingSubscriptions.add(subscribeAction)
        }
    }

    fun sendMessage(roomId: Long, content: String) {
        if (!stompConnected) {
            Log.w(TAG, "Cannot send message: STOMP not connected")
            return
        }

        val messageRequest = ChatMessageWebSocketRequest(content)
        val jsonBody = gson.toJson(messageRequest)

        val sendFrame = buildString {
            append("SEND\n")
            append("destination:/app/chat/$roomId/send\n")
            append("content-type:application/json\n")
            append("\n")
            append(jsonBody)
            append("\u0000")
        }

        webSocket?.send(sendFrame)
        Log.d(TAG, "Sent message to /app/chat/$roomId/send: $jsonBody")
    }

    fun disconnect() {
        stompConnected = false
        pendingSubscriptions.clear()

        webSocket?.let { socket ->
            val disconnectFrame = buildString {
                append("DISCONNECT\n")
                append("\n")
                append("\u0000")
            }
            socket.send(disconnectFrame)
            socket.close(1000, "User disconnected")
        }
        webSocket = null
        currentRoomId = null
        currentMemberId = null
        _connectionState.value = WebSocketConnectionState.Disconnected
        Log.d(TAG, "Disconnected from WebSocket")
    }
}
