import SwiftUI

struct MessageView: View {
    var viewModel: MessageViewModel  // @State 제거 - 외부에서 주입받음
    @State private var showDeleteDialog = false
    @State private var roomToDelete: ChatRoomModel? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("쪽지")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .background(Color.clear)

            // Content
            ZStack {
                if viewModel.uiState.isLoading && viewModel.uiState.chatRooms.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.uiState.error {
                    VStack(spacing: 8) {
                        Text("오류가 발생했습니다")
                            .foregroundStyle(AppColors.error)
                        Button("다시 시도") {
                            Task { await viewModel.loadChatRooms() }
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.uiState.chatRooms.isEmpty {
                    Text("쪽지가 없습니다")
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 0) {
                            ForEach(viewModel.uiState.chatRooms) { room in
                                NavigationLink(value: AppRoute.messageDetail(roomId: room.roomId)) {
                                    ChatRoomListItem(room: room)
                                }
                                .buttonStyle(.plain)
                                .contextMenu {
                                    Button(role: .destructive) {
                                        roomToDelete = room
                                        showDeleteDialog = true
                                    } label: {
                                        Label("삭제", systemImage: "trash")
                                    }
                                }

                                Divider()
                                    .padding(.horizontal, 20)
                            }
                        }
                        .padding(.bottom, 100)
                    }
                    .refreshable {
                        await viewModel.loadChatRooms()
                    }
                }
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .confirmationDialog(
            "쪽지 삭제",
            isPresented: $showDeleteDialog,
            titleVisibility: .visible,
            presenting: roomToDelete
        ) { room in
            Button("삭제", role: .destructive) {
                Task { await viewModel.exitChatRoom(room.roomId) }
            }
            Button("취소", role: .cancel) {}
        } message: { room in
            Text("'\(room.displayName)'님과의 쪽지를 삭제하시겠습니까?")
        }
        .task {
            await viewModel.loadInitialDataIfNeeded()
        }
        .onAppear {
            viewModel.startPolling()
            viewModel.setupChatListUpdateListener()
        }
        .onDisappear {
            viewModel.stopPolling()
        }
    }
}

// MARK: - Chat Room List Item

private struct ChatRoomListItem: View {
    let room: ChatRoomModel

    var body: some View {
        HStack(spacing: 16) {
            // Profile icon
            Image(systemName: "person.circle.fill")
                .resizable()
                .frame(width: 60, height: 60)
                .foregroundStyle(AppColors.onSurface.opacity(0.3))

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(room.displayName)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                        .lineLimit(1)

                    Spacer()

                    if let time = room.lastMessageAt {
                        Text(time.toRelativeTimeText())
                            .font(.caption)
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    }
                }

                Text(room.lastMessage ?? "메시지가 없습니다")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.7))
                    .lineLimit(1)
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 20)
        .background(AppColors.background)
    }
}
