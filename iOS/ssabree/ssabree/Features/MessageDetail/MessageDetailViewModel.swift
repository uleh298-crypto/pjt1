import Foundation
import Observation

// MARK: - Chat Message UI Model

struct ChatMessageUiModel: Identifiable, Equatable {
    let id: Int
    let senderName: String
    let content: String
    let time: String
    let isMe: Bool  // 서버에서 제공하는 본인 메시지 여부
    var isPending: Bool = false  // 전송 중인 메시지 (아직 서버 응답 없음)
}

// MARK: - Message Detail UI State

struct MessageDetailUiState {
    var roomId: Int = 0
    var chatRoom: ChatRoomModel? = nil
    var messages: [ChatMessageUiModel] = []
    var isLoading: Bool = false
    var error: String? = nil
    var messageText: String = ""
    var isSending: Bool = false
    var connectionState: WebSocketConnectionState = .disconnected
    var isNewChatMode: Bool = false
    var pendingPostId: Int? = nil
    var isExiting: Bool = false
    var exitSuccess: Bool = false
}

// MARK: - Message Detail ViewModel

@Observable
@MainActor
final class MessageDetailViewModel {
    private let chatRepository: ChatRepository
    private let authDataStore: AuthDataStore
    private let authRepository: AuthRepository
    private let reportRepository: ReportRepository
    var uiState = MessageDetailUiState()

    private var currentMemberId: Int = 0  // WebSocket 연결용
    private var pendingMessageId: Int = -1  // 임시 메시지 ID (음수로 시작)

    init(chatRepository: ChatRepository, authDataStore: AuthDataStore, authRepository: AuthRepository, reportRepository: ReportRepository) {
        self.chatRepository = chatRepository
        self.authDataStore = authDataStore
        self.authRepository = authRepository
        self.reportRepository = reportRepository

        // 로컬에 저장된 uid가 있으면 먼저 설정 (API 호출 전 빠른 초기화)
        if let uid = authDataStore.getUid() {
            currentMemberId = uid
        }

        setupWebSocketCallbacks()
    }

    private func setupWebSocketCallbacks() {
        ChatWebSocketManager.shared.onConnectionStateChanged = { [weak self] state in
            Task { @MainActor in
                self?.uiState.connectionState = state
            }
        }
        ChatWebSocketManager.shared.onMessageReceived = { [weak self] response in
            Task { @MainActor in
                self?.handleIncomingMessage(response)
            }
        }
    }

    private func handleIncomingMessage(_ response: ChatMessageResponse) {
        let uiMessage = ChatMessageUiModel(
            id: response.messageId,
            senderName: response.senderName ?? "익명",
            content: response.content,
            time: formatChatTime(response.sentAt),
            isMe: response.isMine  // 서버에서 제공하는 본인 메시지 여부 사용
        )

        // Check for duplicates (실제 서버 메시지만 체크, pending 메시지는 제외)
        let existingIds = Set(uiState.messages.filter { !$0.isPending }.map { $0.id })
        if !existingIds.contains(uiMessage.id) {
            // 내 메시지인 경우 pending 메시지 중 같은 내용을 찾아 제거
            if response.isMine {
                if let pendingIndex = uiState.messages.firstIndex(where: { $0.isPending && $0.content == response.content }) {
                    uiState.messages.remove(at: pendingIndex)
                }
            }
            uiState.messages.append(uiMessage)
            print("[MessageDetailVM] Added message \(uiMessage.id)")
        } else {
            print("[MessageDetailVM] Message \(uiMessage.id) already exists, skipping")
        }
    }

    // MARK: - Fetch Member ID (Android의 memberRepository.getMyMemberId() 패턴)

    private func fetchMemberIdIfNeeded() async {
        guard currentMemberId == 0 else { return }

        let result = await authRepository.getMyMemberId()
        if case .success(let memberId) = result {
            currentMemberId = memberId
            print("[MessageDetailVM] Fetched memberId from API: \(memberId)")
        } else {
            print("[MessageDetailVM] Failed to fetch memberId from API")
        }
    }

    // MARK: - Load Existing Chat Room

    func loadChatRoom(roomId: Int) async {
        uiState.roomId = roomId
        uiState.isLoading = true
        uiState.error = nil
        uiState.isNewChatMode = false

        // memberId, 채팅방 정보, 메시지 목록을 병렬로 로드
        async let memberIdFetch: Void = fetchMemberIdIfNeeded()
        async let roomResult = chatRepository.getChatRoom(roomId: roomId)
        async let messagesResult = chatRepository.getMessages(roomId: roomId)

        _ = await memberIdFetch
        let room = await roomResult
        let messages = await messagesResult

        switch room {
        case .success(let chatRoom):
            uiState.chatRoom = chatRoom
            uiState.isLoading = false

            if case .success(let msgList) = messages {
                uiState.messages = msgList.map { msg in
                    ChatMessageUiModel(
                        id: msg.messageId,
                        senderName: msg.senderName,
                        content: msg.content,
                        time: formatChatTime(msg.sentAt),
                        isMe: msg.isMine
                    )
                }
            }

            connectWebSocket(roomId: roomId)
        case .failure(let error):
            uiState.isLoading = false
            uiState.error = error.localizedDescription
        }
    }

    // MARK: - Init New Chat Room (from post)

    func initNewChatRoom(postId: Int) async {
        uiState.isLoading = true
        uiState.error = nil

        // memberId를 API에서 가져오기 (로컬에 없는 경우)
        await fetchMemberIdIfNeeded()

        // Check if room already exists for this post
        let result = await chatRepository.findRoomByPostId(postId: postId)

        switch result {
        case .success(let existingRoom):
            if let room = existingRoom {
                // Room exists - load it
                uiState.roomId = room.roomId
                uiState.chatRoom = room
                uiState.isNewChatMode = false
                uiState.isLoading = false
                await loadMessages(roomId: room.roomId)
                connectWebSocket(roomId: room.roomId)
            } else {
                // No room - enter new chat mode
                uiState.isNewChatMode = true
                uiState.pendingPostId = postId
                uiState.isLoading = false
            }
        case .failure:
            // On failure, still enter new chat mode
            uiState.isNewChatMode = true
            uiState.pendingPostId = postId
            uiState.isLoading = false
        }
    }

    // MARK: - Load Messages

    private func loadMessages(roomId: Int) async {
        let result = await chatRepository.getMessages(roomId: roomId)

        switch result {
        case .success(let messages):
            uiState.messages = messages.map { msg in
                ChatMessageUiModel(
                    id: msg.messageId,
                    senderName: msg.senderName,
                    content: msg.content,
                    time: formatChatTime(msg.sentAt),
                    isMe: msg.isMine  // 서버에서 제공하는 본인 메시지 여부 사용
                )
            }
        case .failure(let error):
            uiState.error = error.localizedDescription
        }
    }

    // MARK: - WebSocket Connection

    private func connectWebSocket(roomId: Int) {
        guard currentMemberId > 0 else {
            print("[MessageDetailVM] Cannot connect WebSocket: memberId is 0")
            return
        }
        print("[MessageDetailVM] Connecting WebSocket: roomId=\(roomId), memberId=\(currentMemberId)")
        ChatWebSocketManager.shared.connect(roomId: roomId, memberId: currentMemberId)
        ChatRoomPresence.shared.enter(roomId: roomId)
    }

    // MARK: - Send Message

    func onMessageTextChange(_ text: String) {
        if text.count > 255 {
            uiState.messageText = String(text.prefix(255))
        } else {
            uiState.messageText = text
        }
    }

    func sendMessage() async {
        let text = uiState.messageText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }

        // New chat mode - create room first
        if uiState.isNewChatMode {
            guard let postId = uiState.pendingPostId else { return }

            uiState.isSending = true
            uiState.messageText = ""

            // 즉시 UI에 메시지 추가 (Optimistic UI)
            let tempId = pendingMessageId
            pendingMessageId -= 1
            let optimisticMessage = ChatMessageUiModel(
                id: tempId,
                senderName: "나",
                content: text,
                time: formatChatTime(getCurrentTimeString()),
                isMe: true,
                isPending: true
            )
            uiState.messages.append(optimisticMessage)

            // 1. Create chat room (백엔드에서 게시글 작성자를 자동으로 대상으로 설정)
            let createResult = await chatRepository.createChatRoom(
                info: ChatRoomCreateInfo(postId: postId)
            )

            switch createResult {
            case .success(let newRoomId):
                uiState.roomId = newRoomId
                uiState.isNewChatMode = false
                uiState.pendingPostId = nil

                // 2. Get chat room info
                if case .success(let room) = await chatRepository.getChatRoom(roomId: newRoomId) {
                    uiState.chatRoom = room
                }

                // 3. Send message via HTTP
                let sendResult = await chatRepository.sendMessage(roomId: newRoomId, content: text)

                switch sendResult {
                case .success:
                    uiState.isSending = false
                    // 임시 메시지 제거 후 서버 메시지로 교체
                    uiState.messages.removeAll { $0.id == tempId }
                    await loadMessages(roomId: newRoomId)
                    connectWebSocket(roomId: newRoomId)
                case .failure(let error):
                    uiState.isSending = false
                    uiState.error = error.localizedDescription
                    // 실패 시 임시 메시지 제거
                    uiState.messages.removeAll { $0.id == tempId }
                }

            case .failure(let error):
                uiState.isSending = false
                uiState.error = error.localizedDescription
                uiState.messageText = text
                // 실패 시 임시 메시지 제거
                uiState.messages.removeAll { $0.id == tempId }
            }
            return
        }

        // Existing room
        let roomId = uiState.roomId
        guard roomId > 0 else { return }

        uiState.messageText = ""

        // 즉시 UI에 메시지 추가 (Optimistic UI)
        let tempId = pendingMessageId
        pendingMessageId -= 1
        let optimisticMessage = ChatMessageUiModel(
            id: tempId,
            senderName: "나",
            content: text,
            time: formatChatTime(getCurrentTimeString()),
            isMe: true,
            isPending: true
        )
        uiState.messages.append(optimisticMessage)

        if case .connected = uiState.connectionState {
            // WebSocket connected - send via WebSocket
            // WebSocket 응답이 오면 handleIncomingMessage에서 처리됨
            ChatWebSocketManager.shared.sendMessage(roomId: roomId, content: text)
            // WebSocket은 응답으로 메시지가 오므로 임시 메시지는 그때 교체됨
        } else {
            // WebSocket not connected - send via HTTP
            let result = await chatRepository.sendMessage(roomId: roomId, content: text)

            switch result {
            case .success:
                // 임시 메시지 제거 후 서버 메시지로 교체
                uiState.messages.removeAll { $0.id == tempId }
                await loadMessages(roomId: roomId)
            case .failure(let error):
                uiState.error = error.localizedDescription
                // 실패 시 임시 메시지 제거
                uiState.messages.removeAll { $0.id == tempId }
            }
        }
    }

    /// 현재 시간을 서버 포맷 문자열로 반환
    private func getCurrentTimeString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        return formatter.string(from: Date())
    }

    // MARK: - Exit Chat Room

    func exitChatRoom() async {
        let roomId = uiState.roomId
        guard roomId > 0 else { return }

        uiState.isExiting = true

        let result = await chatRepository.exitChatRoom(roomId: roomId)

        switch result {
        case .success:
            ChatWebSocketManager.shared.disconnect()
            uiState.isExiting = false
            uiState.exitSuccess = true
        case .failure(let error):
            uiState.isExiting = false
            uiState.error = error.localizedDescription
        }
    }

    // MARK: - Helpers

    func clearError() {
        uiState.error = nil
    }

    func disconnect() {
        ChatWebSocketManager.shared.disconnect()
    }

    // MARK: - Report

    func reportUser(targetMemberId: Int, reason: ReportReason, detail: String?) async {
        let result = await reportRepository.createReport(
            targetType: .user,
            targetId: targetMemberId,
            reason: reason,
            detail: detail
        )

        switch result {
        case .success:
            print("User reported successfully")
        case .failure(let error):
            print("Report user failed: \(error)")
            uiState.error = "신고에 실패했습니다."
        }
    }
}
