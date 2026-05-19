import SwiftUI

struct WriteAnnouncementView: View {
    let groupId: Int
    let groupKind: GroupKind
    @State var viewModel: WriteAnnouncementViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var title: String = ""
    @State private var content: String = ""
    @State private var isPinned: Bool = false

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
                Text("공지사항 작성")
                    .font(.headline)
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
                Button(action: {
                    Task {
                        await viewModel.createAnnouncement(groupId: groupId, groupKind: groupKind, title: title, content: content, isPinned: isPinned)
                    }
                }) {
                    Text("완료")
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundStyle(title.isEmpty || viewModel.uiState.isPosting ? Color.gray : AppColors.primary)
                }
                .disabled(title.isEmpty || viewModel.uiState.isPosting)
            }
            .padding()
            .background(AppColors.surface)

            ScrollView {
                VStack(spacing: 16) {
                    TextField("제목을 입력하세요", text: $title)
                        .font(.body)
                        .padding()
                        .background(AppColors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                    ZStack(alignment: .topLeading) {
                        if content.isEmpty {
                            Text("내용을 입력하세요")
                                .foregroundStyle(Color.gray.opacity(0.6))
                                .padding(.top, 12)
                                .padding(.leading, 12)
                        }
                        TextEditor(text: $content)
                            .frame(minHeight: 300)
                            .padding(8)
                            .scrollContentBackground(.hidden)
                            .background(AppColors.surface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Toggle(isOn: $isPinned) {
                        HStack {
                            Image(systemName: "pin.fill")
                                .foregroundStyle(isPinned ? AppColors.primary : AppColors.onSurfaceVariant)
                            Text("상단 고정")
                                .foregroundStyle(AppColors.onSurface)
                        }
                    }
                    .padding()
                    .background(AppColors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding()
            }
            .background(AppColors.background)
        }
        .navigationBarBackButtonHidden(true)
        .overlay {
            if viewModel.uiState.isPosting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
            }
        }
        .onChange(of: viewModel.uiState.isSuccess) { _, isSuccess in
            if isSuccess { dismiss() }
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
