import Foundation

// MARK: - WebSocket Connection State

enum WebSocketConnectionState: Equatable {
    case disconnected
    case connecting
    case connected
    case error(String)
}

// MARK: - Chat WebSocket Manager

@MainActor
final class ChatWebSocketManager: NSObject {
    static let shared = ChatWebSocketManager()

    // MARK: - Public State & Callbacks

    private(set) var connectionState: WebSocketConnectionState = .disconnected {
        didSet {
            guard oldValue != connectionState else { return }
            print("[WS] State: \(oldValue) → \(connectionState)")
            onConnectionStateChanged?(connectionState)
        }
    }

    var onConnectionStateChanged: ((WebSocketConnectionState) -> Void)?
    var onMessageReceived: ((ChatMessageResponse) -> Void)?
    var onChatListUpdate: ((ChatListUpdateResponse) -> Void)?

    // MARK: - Private: Connection

    private var webSocketTask: URLSessionWebSocketTask?
    private var session: URLSession?
    private var receiveTask: Task<Void, Never>?

    // MARK: - Private: Room Info (kept for reconnection)

    private var currentRoomId: Int?
    private var currentMemberId: Int?

    // MARK: - Private: STOMP

    private var stompConnected = false

    // MARK: - Private: Timers

    private var heartbeatTimer: Timer?

    // MARK: - Private: Auth

    private var authDataStore: AuthDataStore?

    // MARK: - Private: Reconnection

    /// true during intentional cleanup — suppresses auto-reconnect
    private var isIntentionalDisconnect = false
    private var reconnectTask: Task<Void, Never>?
    private var reconnectAttempts = 0
    private static let maxReconnectAttempts = 5

    // MARK: - Init

    private override init() {
        super.init()
    }

    func configure(authDataStore: AuthDataStore) {
        self.authDataStore = authDataStore
    }

    // MARK: - Public: Connect

    func connect(roomId: Int, memberId: Int) {
        // Skip if already STOMP-connected to the same room
        if stompConnected,
           currentRoomId == roomId,
           currentMemberId == memberId {
            print("[WS] Already STOMP-connected to room \(roomId), skipping")
            return
        }

        print("[WS] connect(roomId: \(roomId), memberId: \(memberId))")

        // 1) Cleanup old connection — flag prevents delegate-triggered reconnect
        isIntentionalDisconnect = true
        cleanupConnection()
        isIntentionalDisconnect = false

        // 2) Store room info
        currentRoomId = roomId
        currentMemberId = memberId
        connectionState = .connecting

        // 3) Build WebSocket URL
        let baseUrl = APIClient.baseURL
            .replacingOccurrences(of: "http://", with: "ws://")
            .replacingOccurrences(of: "https://", with: "wss://")
            .trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let wsUrl = "\(baseUrl)/ws-stomp"

        guard let url = URL(string: wsUrl) else {
            print("[WS] ERROR: Invalid URL — \(wsUrl)")
            connectionState = .error("Invalid URL")
            return
        }

        print("[WS] Opening WebSocket → \(wsUrl)")

        // 4) Build request with auth header
        var request = URLRequest(url: url)
        request.timeoutInterval = 30

        if let token = authDataStore?.getAccessToken() {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
            print("[WS] Auth token attached (len=\(token.count))")
        } else {
            print("[WS] ⚠️ No auth token available — STOMP CONNECT will likely fail")
        }

        // 5) Create URLSession + WebSocketTask
        let config = URLSessionConfiguration.default
        session = URLSession(configuration: config, delegate: self, delegateQueue: .main)
        webSocketTask = session?.webSocketTask(with: request)
        webSocketTask?.resume()

        // 6) Start async receive loop
        startReceiveLoop()
    }

    // MARK: - Public: Disconnect

    func disconnect() {
        print("[WS] disconnect() — intentional")
        isIntentionalDisconnect = true

        // Send STOMP DISCONNECT before tearing down
        if stompConnected {
            sendFrame("DISCONNECT\n\n\0")
        }

        cleanupConnection()
        currentRoomId = nil
        currentMemberId = nil
        connectionState = .disconnected
    }

    // MARK: - Public: Send Chat Message

    func sendMessage(roomId: Int, content: String) {
        guard stompConnected else {
            print("[WS] Cannot send message — STOMP not connected")
            return
        }

        guard let jsonData = try? JSONEncoder().encode(ChatMessageWebSocketRequest(content: content)),
              let jsonBody = String(data: jsonData, encoding: .utf8) else {
            print("[WS] Failed to encode message")
            return
        }

        var frame = "SEND\n"
        frame += "destination:/app/chat/\(roomId)/send\n"
        frame += "content-type:application/json\n"
        frame += "\n"
        frame += jsonBody
        frame += "\0"

        sendFrame(frame)
        print("[WS] Sent message to /app/chat/\(roomId)/send")
    }

    // MARK: - Private: Resource Cleanup (no state change)

    private func cleanupConnection() {
        // Cancel async tasks
        receiveTask?.cancel()
        receiveTask = nil
        reconnectTask?.cancel()
        reconnectTask = nil

        // Stop heartbeat
        heartbeatTimer?.invalidate()
        heartbeatTimer = nil

        // Reset STOMP flag
        stompConnected = false

        // Tear down WebSocket
        webSocketTask?.cancel(with: .goingAway, reason: nil)
        webSocketTask = nil
        session?.invalidateAndCancel()
        session = nil
    }

    // MARK: - Private: Async Receive Loop

    private func startReceiveLoop() {
        receiveTask = Task { [weak self] in
            guard let self = self, let task = self.webSocketTask else { return }

            while !Task.isCancelled {
                do {
                    let message = try await task.receive()

                    switch message {
                    case .string(let text):
                        self.handleStompFrame(text)
                    case .data(let data):
                        if let text = String(data: data, encoding: .utf8) {
                            self.handleStompFrame(text)
                        }
                    @unknown default:
                        break
                    }
                } catch {
                    // Only reconnect if this wasn't intentional
                    if !Task.isCancelled && !self.isIntentionalDisconnect {
                        print("[WS] Receive loop error: \(error.localizedDescription)")
                        self.handleConnectionLost()
                    }
                    return
                }
            }
        }
    }

    // MARK: - Private: Connection Lost → Auto-Reconnect

    private func handleConnectionLost() {
        guard !isIntentionalDisconnect else { return }

        // Prevent duplicate reconnect scheduling
        if let task = reconnectTask, !task.isCancelled { return }

        stompConnected = false
        heartbeatTimer?.invalidate()
        heartbeatTimer = nil
        connectionState = .disconnected

        guard let roomId = currentRoomId, let memberId = currentMemberId else {
            print("[WS] Cannot reconnect — no room/member info")
            return
        }

        guard reconnectAttempts < Self.maxReconnectAttempts else {
            print("[WS] Reconnect limit reached (\(Self.maxReconnectAttempts))")
            return
        }

        reconnectAttempts += 1
        let delaySec = reconnectAttempts * 2
        print("[WS] Reconnect \(reconnectAttempts)/\(Self.maxReconnectAttempts) in \(delaySec)s …")

        reconnectTask = Task { [weak self] in
            try? await Task.sleep(nanoseconds: UInt64(delaySec) * 1_000_000_000)
            guard let self = self, !Task.isCancelled else { return }
            self.connect(roomId: roomId, memberId: memberId)
        }
    }

    // MARK: - STOMP: Frame Router

    private func handleStompFrame(_ raw: String) {
        // Normalize CRLF → LF
        let frame = raw.replacingOccurrences(of: "\r\n", with: "\n")

        if frame.hasPrefix("CONNECTED") {
            print("[WS] ✓ STOMP CONNECTED")
            stompConnected = true
            reconnectAttempts = 0
            connectionState = .connected

            // Subscribe to room messages + chat-list updates
            if let roomId = currentRoomId, let memberId = currentMemberId {
                subscribeToRoom(roomId: roomId, memberId: memberId)
                subscribeToChatList(memberId: memberId)
            }

            startHeartbeat()

        } else if frame.hasPrefix("MESSAGE") {
            handleMessageFrame(frame)

        } else if frame.hasPrefix("ERROR") {
            print("[WS] ✗ STOMP ERROR:\n\(frame)")
            connectionState = .error(extractBody(from: frame))

        } else if frame == "\n" || frame.isEmpty {
            // Server heartbeat → respond
            sendFrame("\n")
        }
    }

    // MARK: - STOMP: Send Frames

    private func sendConnectFrame() {
        let token = authDataStore?.getAccessToken() ?? ""

        var frame = "CONNECT\n"
        frame += "accept-version:1.1,1.2\n"
        frame += "heart-beat:10000,10000\n"
        if !token.isEmpty {
            frame += "Authorization:Bearer \(token)\n"
        }
        frame += "\n\0"

        sendFrame(frame)
        print("[WS] → STOMP CONNECT (token=\(!token.isEmpty))")
    }

    private func subscribeToRoom(roomId: Int, memberId: Int) {
        var frame = "SUBSCRIBE\n"
        frame += "id:sub-room-\(memberId)-\(roomId)\n"
        frame += "destination:/topic/user/\(memberId)/chat/\(roomId)\n"
        frame += "\n\0"

        sendFrame(frame)
        print("[WS] → SUBSCRIBE /topic/user/\(memberId)/chat/\(roomId)")
    }

    private func subscribeToChatList(memberId: Int) {
        var frame = "SUBSCRIBE\n"
        frame += "id:sub-chatlist-\(memberId)\n"
        frame += "destination:/topic/user/\(memberId)/chat-list\n"
        frame += "\n\0"

        sendFrame(frame)
        print("[WS] → SUBSCRIBE /topic/user/\(memberId)/chat-list")
    }

    // MARK: - STOMP: Parse Incoming MESSAGE

    private func handleMessageFrame(_ frame: String) {
        guard let destination = extractHeader(from: frame, named: "destination") else {
            print("[WS] MESSAGE frame missing destination header")
            return
        }

        let body = extractBody(from: frame)
        guard let data = body.data(using: .utf8), !body.isEmpty else {
            print("[WS] MESSAGE frame has empty body")
            return
        }

        do {
            if destination.contains("/topic/user/")
                && destination.contains("/chat/")
                && !destination.contains("chat-list") {
                // Chat message
                let response = try JSONDecoder().decode(ChatMessageResponse.self, from: data)
                print("[WS] ← Chat message #\(response.messageId) from \(response.senderName ?? "?")")
                onMessageReceived?(response)

            } else if destination.contains("/chat-list") {
                // Chat list update
                let response = try JSONDecoder().decode(ChatListUpdateResponse.self, from: data)
                print("[WS] ← Chat-list update for room \(response.roomId)")
                onChatListUpdate?(response)
            }
        } catch {
            print("[WS] JSON decode error: \(error)")
        }
    }

    // MARK: - Low-Level Helpers

    private func sendFrame(_ text: String) {
        webSocketTask?.send(.string(text)) { error in
            if let error = error {
                print("[WS] Send failed: \(error.localizedDescription)")
            }
        }
    }

    private func startHeartbeat() {
        heartbeatTimer?.invalidate()
        heartbeatTimer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                self?.sendFrame("\n")
            }
        }
    }

    private func extractHeader(from frame: String, named name: String) -> String? {
        for line in frame.split(separator: "\n") {
            let str = String(line)
            if str.hasPrefix("\(name):") {
                return String(str.dropFirst(name.count + 1))
                    .trimmingCharacters(in: .whitespaces)
            }
        }
        return nil
    }

    private func extractBody(from frame: String) -> String {
        guard let range = frame.range(of: "\n\n") else { return "" }
        return String(frame[range.upperBound...])
            .replacingOccurrences(of: "\0", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - URLSessionWebSocketDelegate

extension ChatWebSocketManager: URLSessionWebSocketDelegate {
    nonisolated func urlSession(
        _ session: URLSession,
        webSocketTask: URLSessionWebSocketTask,
        didOpenWithProtocol protocol: String?
    ) {
        Task { @MainActor in
            print("[WS] WebSocket TCP handshake complete — sending STOMP CONNECT")
            self.sendConnectFrame()
        }
    }

    nonisolated func urlSession(
        _ session: URLSession,
        webSocketTask: URLSessionWebSocketTask,
        didCloseWith closeCode: URLSessionWebSocketTask.CloseCode,
        reason: Data?
    ) {
        Task { @MainActor in
            print("[WS] WebSocket closed (code=\(closeCode.rawValue))")
            if !self.isIntentionalDisconnect {
                self.handleConnectionLost()
            }
        }
    }

    nonisolated func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        Task { @MainActor in
            if let error = error {
                print("[WS] URLSession error: \(error.localizedDescription)")
                if !self.isIntentionalDisconnect {
                    self.handleConnectionLost()
                }
            }
        }
    }
}
