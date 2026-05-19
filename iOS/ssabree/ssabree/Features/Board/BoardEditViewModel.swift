import Foundation
import Observation

// MARK: - UI State

struct BoardEditUiState {
    var postId: Int = 0
    var title: String = ""
    var content: String = ""
    var isLoading: Bool = false
    var isSubmitting: Bool = false
    var isSubmitSuccess: Bool = false
    var submitError: String? = nil

    var isSubmitEnabled: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !isSubmitting
    }
}

// MARK: - ViewModel

@Observable
@MainActor
final class BoardEditViewModel {
    private let postRepository: PostRepository

    var uiState = BoardEditUiState()

    init(postRepository: PostRepository) {
        self.postRepository = postRepository
    }

    // MARK: - Load Post

    func loadPost(postId: Int) async {
        uiState.postId = postId
        uiState.isLoading = true

        let result = await postRepository.getPostDetail(postId: postId)

        switch result {
        case .success(let detail):
            uiState.title = detail.title
            uiState.content = detail.content
        case .failure(let error):
            print("Failed to load post: \(error)")
            uiState.submitError = "게시글을 불러오는데 실패했습니다."
        }

        uiState.isLoading = false
    }

    // MARK: - Title & Content

    func onTitleChange(_ value: String) {
        uiState.title = value
    }

    func onContentChange(_ value: String) {
        uiState.content = value
    }

    // MARK: - Submit

    func onSubmit() async {
        guard uiState.isSubmitEnabled else { return }

        uiState.isSubmitting = true
        uiState.submitError = nil

        let updateInfo = PostUpdateInfo(
            title: uiState.title.trimmingCharacters(in: .whitespacesAndNewlines),
            content: uiState.content.trimmingCharacters(in: .whitespacesAndNewlines)
        )

        let result = await postRepository.updatePost(postId: uiState.postId, post: updateInfo)

        switch result {
        case .success:
            uiState.isSubmitSuccess = true
        case .failure(let error):
            print("Failed to update post: \(error)")
            uiState.submitError = "게시글 수정에 실패했습니다."
        }

        uiState.isSubmitting = false
    }

    func clearSubmitError() {
        uiState.submitError = nil
    }

    func clearSubmitSuccess() {
        uiState.isSubmitSuccess = false
    }
}
