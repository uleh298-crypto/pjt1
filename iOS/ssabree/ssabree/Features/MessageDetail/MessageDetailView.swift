import SwiftUI

struct MessageDetailView: View {
    let roomId: Int
    let postId: Int?       // 새 채팅용: 게시글 ID
    @State var viewModel: MessageDetailViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var isMenuExpanded = false
    @State private var showExitDialog = false

    // 기존 채팅방용 init
    init(roomId: Int, viewModel: MessageDetailViewModel) {
        self.roomId = roomId
        self.postId = nil
        self._viewModel = State(initialValue: viewModel)
    }

    // 새 채팅용 init (게시글에서 쪽지 보내기)
    init(postId: Int, viewModel: MessageDetailViewModel) {
        self.roomId = 0
        self.postId = postId
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top Bar
            topBar

            // Chat Content
            ZStack {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.uiState.error {
                    VStack(spacing: 8) {
                        Text("오류가 발생했습니다")
                            .foregroundStyle(AppColors.error)
                        Button("다시 시도") {
                            Task { await viewModel.loadChatRoom(roomId: roomId) }
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.uiState.messages.isEmpty {
                    Text(viewModel.uiState.isNewChatMode
                         ? "첫 메시지를 보내 대화를 시작하세요!"
                         : "메시지가 없습니다.\n첫 메시지를 보내보세요!")
                        .multilineTextAlignment(.center)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    messageList
                }
            }
            .background(AppColors.background)

            // Input Area
            inputArea
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            if let postId = postId {
                // 새 채팅 모드 (백엔드에서 게시글 작성자를 대상으로 자동 설정)
                await viewModel.initNewChatRoom(postId: postId)
            } else {
                // 기존 채팅방
                await viewModel.loadChatRoom(roomId: roomId)
            }
        }
        .onChange(of: viewModel.uiState.exitSuccess) { _, success in
            if success {
                dismiss()
            }
        }
        .confirmationDialog(
            "채팅방 나가기",
            isPresented: $showExitDialog,
            titleVisibility: .visible
        ) {
            Button("나가기", role: .destructive) {
                Task { await viewModel.exitChatRoom() }
            }
            Button("취소", role: .cancel) {}
        } message: {
            Text("정말로 채팅방을 나가시겠습니까?\n나가면 대화 내용이 삭제됩니다.")
        }
        .onAppear {
            // 채팅방 활성 상태 등록 (알림 억제용)
            if roomId > 0 {
                ChatRoomPresence.shared.enter(roomId: roomId)
            }
        }
        .onDisappear {
            // 채팅방 활성 상태 해제
            ChatRoomPresence.shared.exit(roomId: roomId > 0 ? roomId : nil)
            viewModel.disconnect()
        }
    }

    // MARK: - Top Bar

    private var topBar: some View {
        HStack(spacing: 0) {
            Button {
                dismiss()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onSurface)
            }
            .padding(.leading, 16)

            VStack(alignment: .leading, spacing: 2) {
                Text(titleText)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                if viewModel.uiState.isNewChatMode {
                    Text("메시지를 보내면 채팅방이 생성됩니다")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                } else if viewModel.uiState.chatRoom?.isDeleted == true {
                    Text("연결 끊김")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                } else if case .connected = viewModel.uiState.connectionState {
                    Text("연결됨")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.primary.opacity(0.7))
                } else if case .connecting = viewModel.uiState.connectionState {
                    Text("연결 중...")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                } else {
                    Text("연결 끊김")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }
            }
            .padding(.leading, 12)

            Spacer()

            Menu {
                if !viewModel.uiState.isNewChatMode && viewModel.uiState.roomId > 0 {
                    Button(role: .destructive) {
                        showExitDialog = true
                    } label: {
                        Label("채팅방 나가기", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.title3)
                    .foregroundStyle(AppColors.onSurface)
                    .padding(16)
            }
        }
        .background(AppColors.background)
    }

    private var titleText: String {
        if viewModel.uiState.isNewChatMode {
            return "새 쪽지"
        } else if let chatRoom = viewModel.uiState.chatRoom,
                  !chatRoom.chatRoomName.isEmpty {
            return chatRoom.chatRoomName
        } else {
            return "쪽지"
        }
    }

    // MARK: - Message List

    private var messageList: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 24) {
                    ForEach(viewModel.uiState.messages) { message in
                        ChatBubble(message: message)
                            .id(message.id)
                    }

                    // 하단 앵커
                    Color.clear
                        .frame(height: 1)
                        .id("bottom")
                }
                .padding(20)
            }
            .defaultScrollAnchor(.bottom)
            .onAppear {
                // LazyVStack 레이아웃 완료 후 하단으로 스크롤
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                    proxy.scrollTo("bottom", anchor: .bottom)
                }
            }
            .onChange(of: viewModel.uiState.messages.count) { _, newCount in
                guard newCount > 0 else { return }
                withAnimation(.easeOut(duration: 0.2)) {
                    proxy.scrollTo("bottom", anchor: .bottom)
                }
            }
        }
    }

    // MARK: - Input Area

    private var inputArea: some View {
        VStack(spacing: 0) {
            Divider()

            // 글자 수 카운터
            if !viewModel.uiState.messageText.isEmpty {
                HStack {
                    Spacer()
                    Text("\(viewModel.uiState.messageText.count)/255")
                        .font(.system(size: 11))
                        .foregroundStyle(
                            viewModel.uiState.messageText.count >= 255
                                ? AppColors.error
                                : AppColors.onSurfaceVariant
                        )
                }
                .padding(.horizontal, 24)
                .padding(.top, 6)
            }

            HStack(spacing: 0) {
                TextField("메시지를 입력하세요", text: Binding(
                    get: { viewModel.uiState.messageText },
                    set: { viewModel.onMessageTextChange($0) }
                ))
                .padding(.horizontal, 20)
                .padding(.vertical, 12)

                Button {
                    Task { await viewModel.sendMessage() }
                } label: {
                    Image(systemName: "paperplane.fill")
                        .font(.title3)
                        .foregroundStyle(
                            viewModel.uiState.messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.uiState.isSending
                            ? AppColors.onSurface.opacity(0.3)
                            : AppColors.primary
                        )
                }
                .disabled(viewModel.uiState.messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.uiState.isSending)
                .padding(.trailing, 16)
            }
            .frame(height: 50)
            .background(AppColors.surfaceVariant.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 25))
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .background(AppColors.background)
    }
}

// MARK: - Chat Bubble

private struct ChatBubble: View {
    let message: ChatMessageUiModel

    var body: some View {
        if message.isMe {
            // My message - right aligned
            HStack(alignment: .bottom, spacing: 8) {
                Spacer()

                Text(message.time)
                    .font(.system(size: 12))
                    .foregroundStyle(Color.gray)

                Text(message.content)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .font(.system(size: 15))
                    .foregroundStyle(AppColors.onSurface)
                    .background(AppColors.primaryContainer.opacity(0.3))
                    .clipShape(
                        .rect(
                            topLeadingRadius: 16,
                            bottomLeadingRadius: 16,
                            bottomTrailingRadius: 4,
                            topTrailingRadius: 16
                        )
                    )
            }
        } else {
            // Other's message - left aligned
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .frame(width: 50, height: 50)
                    .foregroundStyle(AppColors.onSurface.opacity(0.3))

                VStack(alignment: .leading, spacing: 4) {
                    Text(message.senderName)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    HStack(alignment: .bottom, spacing: 8) {
                        Text(message.content)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .font(.system(size: 15))
                            .foregroundStyle(AppColors.onSurface)
                            .background(AppColors.surfaceVariant.opacity(0.3))
                            .clipShape(
                                .rect(
                                    topLeadingRadius: 4,
                                    bottomLeadingRadius: 16,
                                    bottomTrailingRadius: 16,
                                    topTrailingRadius: 16
                                )
                            )

                        Text(message.time)
                            .font(.system(size: 12))
                            .foregroundStyle(Color.gray)
                    }
                }

                Spacer()
            }
        }
    }
}
