import SwiftUI

// MARK: - MyComments UI State

struct MyCommentsUiState {
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var comments: [MyCommentModel] = []
}

// MARK: - MyComments ViewModel

@Observable
final class MyCommentsViewModel {
    private let myPageRepository: MyPageRepository

    private(set) var uiState = MyCommentsUiState()

    init(myPageRepository: MyPageRepository) {
        self.myPageRepository = myPageRepository
    }

    @MainActor
    func loadMyComments() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result = await myPageRepository.getMyComments()

        switch result {
        case .success(let comments):
            uiState.comments = comments
            uiState.isLoading = false
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }
}

// MARK: - MyComments View

struct MyCommentsView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: MyCommentsViewModel
    let onCommentTap: (Int, Int) -> Void  // (boardId, postId)

    init(viewModel: MyCommentsViewModel, onCommentTap: @escaping (Int, Int) -> Void) {
        self._viewModel = State(initialValue: viewModel)
        self.onCommentTap = onCommentTap
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }

                Spacer()

                Text("작성한 댓글")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                Spacer()

                // Balance element
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(.clear)
            }
            .padding()
            .background(AppColors.background)

            // Content
            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let error = viewModel.uiState.errorMessage {
                Spacer()
                Text(error)
                    .foregroundStyle(AppColors.error)
                Spacer()
            } else if viewModel.uiState.comments.isEmpty {
                Spacer()
                Text("작성한 댓글이 없습니다")
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.uiState.comments) { comment in
                            CommentListItemView(comment: comment)
                                .onTapGesture {
                                    onCommentTap(comment.boardId, comment.postId)
                                }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadMyComments()
        }
    }
}

// MARK: - Comment List Item View

private struct CommentListItemView: View {
    let comment: MyCommentModel

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Board name + Reply badge
            HStack(spacing: 8) {
                Text(comment.boardName)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(AppColors.primary)

                if comment.isReply {
                    Text("답글")
                        .font(.system(size: 10))
                        .foregroundStyle(AppColors.primary)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(AppColors.primary.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }

            Spacer().frame(height: 4)

            // Post title
            Text(comment.postTitle)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
                .lineLimit(1)

            Spacer().frame(height: 8)

            // Comment content
            Text(comment.content)
                .font(.system(size: 15))
                .foregroundStyle(AppColors.onSurface)
                .lineLimit(3)

            // Date
            if let createdAt = comment.createdAt, !createdAt.isEmpty {
                Spacer().frame(height: 8)
                Text(String(createdAt.prefix(10)))
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.05), radius: 2, x: 0, y: 1)
    }
}

// MARK: - Preview

#Preview {
    MyCommentsView(
        viewModel: MyCommentsViewModel(myPageRepository: FakeMyPageRepository()),
        onCommentTap: { _, _ in }
    )
}
