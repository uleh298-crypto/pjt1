import SwiftUI

// MARK: - MyPosts UI State

struct MyPostsUiState {
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var posts: [PostModel] = []
}

// MARK: - MyPosts ViewModel

@Observable
final class MyPostsViewModel {
    private let myPageRepository: MyPageRepository

    private(set) var uiState = MyPostsUiState()

    init(myPageRepository: MyPageRepository) {
        self.myPageRepository = myPageRepository
    }

    @MainActor
    func loadMyPosts() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result = await myPageRepository.getMyPosts()

        switch result {
        case .success(let posts):
            uiState.posts = posts
            uiState.isLoading = false
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }
}

// MARK: - MyPosts View

struct MyPostsView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: MyPostsViewModel
    let onPostTap: (Int, Int) -> Void  // (boardId, postId)

    init(viewModel: MyPostsViewModel, onPostTap: @escaping (Int, Int) -> Void) {
        self._viewModel = State(initialValue: viewModel)
        self.onPostTap = onPostTap
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

                Text("작성한 글")
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
            } else if viewModel.uiState.posts.isEmpty {
                Spacer()
                Text("작성한 글이 없습니다")
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.uiState.posts, id: \.id) { post in
                            PostListItemView(post: post)
                                .onTapGesture {
                                    onPostTap(post.boardId, post.id)
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
            await viewModel.loadMyPosts()
        }
    }
}

// MARK: - Post List Item View

private struct PostListItemView: View {
    let post: PostModel

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(post.boardName)
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(AppColors.primary)

            Spacer().frame(height: 4)

            Text(post.title)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
                .lineLimit(1)

            Spacer().frame(height: 4)

            Text(post.content)
                .font(.system(size: 14))
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
                .lineLimit(2)

            Spacer().frame(height: 8)

            HStack(spacing: 12) {
                Text("좋아요 \(post.likeCount)")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                Text("댓글 \(post.commentCount)")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                Text("조회 \(post.viewCount)")
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
    MyPostsView(
        viewModel: MyPostsViewModel(myPageRepository: FakeMyPageRepository()),
        onPostTap: { _, _ in }
    )
}
