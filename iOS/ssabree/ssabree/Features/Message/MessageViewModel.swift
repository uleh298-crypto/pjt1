import Foundation
import Observation
import Combine

// MARK: - Message UI State

private let pollingIntervalSeconds: TimeInterval = 30

struct MessageUiState {
    var chatRooms: [ChatRoomModel] = []
    var isLoading: Bool = false
    var error: String? = nil
}

// MARK: - Message ViewModel

@Observable
final class MessageViewModel {
    private let chatRepository: ChatRepository
    var uiState = MessageUiState()
    private var hasLoadedInitialData = false
    private var pollTask: Task<Void, Never>?
    private var pushEventCancellable: AnyCancellable?

    init(chatRepository: ChatRepository) {
        self.chatRepository = chatRepository
        observePushEvents()
    }

    /// PushEventBus 구독 - 채팅 푸시 알림 수신 시 채팅방 목록 갱신
    private func observePushEvents() {
        pushEventCancellable = PushEventBus.shared.events
            .sink { [weak self] event in
                if case .openChat = event {
                    Task { @MainActor [weak self] in
                        await self?.loadChatRooms()
                    }
                }
            }
    }

    /// WebSocket chat-list 업데이트 구독 설정
    func setupChatListUpdateListener() {
        ChatWebSocketManager.shared.onChatListUpdate = { [weak self] update in
            Task { @MainActor [weak self] in
                self?.handleChatListUpdate(update)
            }
        }
    }

    /// 로컬 채팅방 목록을 WebSocket 업데이트로 즉시 갱신
    @MainActor
    private func handleChatListUpdate(_ update: ChatListUpdateResponse) {
        if let index = uiState.chatRooms.firstIndex(where: { $0.roomId == update.roomId }) {
            // 기존 방의 마지막 메시지/시간 업데이트
            var room = uiState.chatRooms[index]
            room = ChatRoomModel(
                roomId: room.roomId,
                chatRoomName: room.chatRoomName,
                opponentName: room.opponentName,
                postId: room.postId,
                postTitle: room.postTitle,
                lastMessage: update.lastMessage ?? room.lastMessage,
                lastMessageAt: update.lastMessageAt ?? room.lastMessageAt,
                isDeleted: room.isDeleted,
                createdAt: room.createdAt
            )
            uiState.chatRooms[index] = room
            // 최신 메시지 순으로 재정렬
            uiState.chatRooms.sort { r1, r2 in
                guard let t1 = r1.lastMessageAt, let t2 = r2.lastMessageAt else {
                    return r1.lastMessageAt != nil
                }
                return t1 > t2
            }
        } else {
            // 새 채팅방이면 전체 목록 다시 로드
            Task { @MainActor [weak self] in
                await self?.loadChatRooms()
            }
        }
    }

    /// View의 .task modifier에서 호출 - 최초 1회만 데이터 로드
    /// Task.detached를 사용하여 SwiftUI의 .task cancellation으로부터 보호
    @MainActor
    func loadInitialDataIfNeeded() async {
        guard !hasLoadedInitialData else { return }
        hasLoadedInitialData = true

        await Task.detached { @MainActor [self] in
            await self.loadChatRooms()
        }.value
    }

    @MainActor
    func loadChatRooms() async {
        uiState.isLoading = true
        uiState.error = nil

        let result = await chatRepository.getChatRooms()

        switch result {
        case .success(let rooms):
            // 삭제되지 않은 채팅방만 필터링하고, 최근 메시지 시간 순으로 정렬
            uiState.chatRooms = rooms
                .filter { !$0.isDeleted }
                .sorted { room1, room2 in
                    guard let time1 = room1.lastMessageAt,
                          let time2 = room2.lastMessageAt else {
                        return room1.lastMessageAt != nil
                    }
                    return time1 > time2
                }
            uiState.error = nil
        case .failure(let error):
            uiState.error = error.localizedDescription
        }

        uiState.isLoading = false
    }

    @MainActor
    func exitChatRoom(_ roomId: Int) async {
        let result = await chatRepository.exitChatRoom(roomId: roomId)

        switch result {
        case .success:
            // 로컬에서 채팅방 제거
            uiState.chatRooms.removeAll { $0.roomId == roomId }
        case .failure(let error):
            uiState.error = error.localizedDescription
        }
    }

    // MARK: - Polling

    func startPolling() {
        stopPolling()
        // 즉시 로드 후 폴링 시작 (Android와 동일)
        pollTask = Task { @MainActor [weak self] in
            await self?.loadChatRooms()
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: UInt64(pollingIntervalSeconds * 1_000_000_000))
                await self?.loadChatRooms()
            }
        }
    }

    func stopPolling() {
        pollTask?.cancel()
        pollTask = nil
    }

    func clearError() {
        uiState.error = nil
    }
}
