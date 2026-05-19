import SwiftUI

struct EditAnnouncementView: View {
    let groupId: Int
    let groupKind: GroupKind
    let announcementId: Int
    @State var viewModel: EditAnnouncementViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var showDeleteDialog = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "xmark")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
                Spacer()
                Text("공지사항 수정")
                    .font(.headline)
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
                Button(action: {
                    Task {
                        await viewModel.updateAnnouncement(groupId: groupId, groupKind: groupKind, announcementId: announcementId)
                    }
                }) {
                    Text("완료")
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundStyle(viewModel.uiState.title.isEmpty ? Color.gray : AppColors.primary)
                }
                .disabled(viewModel.uiState.title.isEmpty || viewModel.uiState.isPosting)
            }
            .padding()
            .background(AppColors.surface)

            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        TextField("제목을 입력하세요", text: $viewModel.uiState.title)
                            .font(.body)
                            .padding()
                            .background(AppColors.surface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))

                        ZStack(alignment: .topLeading) {
                            if viewModel.uiState.content.isEmpty {
                                Text("내용을 입력하세요")
                                    .foregroundStyle(Color.gray.opacity(0.6))
                                    .padding(.top, 12)
                                    .padding(.leading, 12)
                            }
                            TextEditor(text: $viewModel.uiState.content)
                                .frame(minHeight: 300)
                                .padding(8)
                                .scrollContentBackground(.hidden)
                                .background(AppColors.surface)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        Toggle(isOn: $viewModel.uiState.isPinned) {
                            HStack {
                                Image(systemName: "pin.fill")
                                    .foregroundStyle(viewModel.uiState.isPinned ? AppColors.primary : AppColors.onSurfaceVariant)
                                Text("상단 고정")
                                    .foregroundStyle(AppColors.onSurface)
                            }
                        }
                        .padding()
                        .background(AppColors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                        Button(action: { showDeleteDialog = true }) {
                            Text("삭제하기")
                                .font(.body)
                                .foregroundStyle(AppColors.error)
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(AppColors.surface)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                    .padding()
                }
                .background(AppColors.background)
            }
        }
        .navigationBarBackButtonHidden(true)
        .overlay {
            if viewModel.uiState.isPosting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
            }
        }
        .task {
            await viewModel.loadAnnouncement(groupId: groupId, groupKind: groupKind, announcementId: announcementId)
        }
        .onChange(of: viewModel.uiState.isSuccess) { _, isSuccess in
            if isSuccess { dismiss() }
        }
        .onChange(of: viewModel.uiState.isDeleted) { _, isDeleted in
            if isDeleted { dismiss() }
        }
        .alert("공지사항 삭제", isPresented: $showDeleteDialog) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task { await viewModel.deleteAnnouncement(groupId: groupId, groupKind: groupKind, announcementId: announcementId) }
            }
        } message: {
            Text("이 공지사항을 삭제하시겠습니까?")
        }
        .alert("오류", isPresented: Binding(
            get: { viewModel.uiState.error != nil },
            set: { if !$0 { viewModel.uiState.error = nil } }
        )) {
            Button("확인", role: .cancel) {}
        } message: {
            if let error = viewModel.uiState.error {
                Text(error)
            }
        }
    }
}
