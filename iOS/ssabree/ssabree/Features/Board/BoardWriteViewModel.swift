import Foundation
import Observation
import UIKit

// MARK: - Vote Option Model

struct VoteOptionModel: Identifiable {
    let id: Int
    var text: String
}

// MARK: - Board Option Model

struct BoardOptionModel: Identifiable {
    let id: Int
    let name: String
}

// MARK: - UI State

struct BoardWriteUiState {
    var title: String = ""
    var content: String = ""
    var attachedImages: [UIImage] = []
    var boardOptions: [BoardOptionModel] = []
    var selectedBoardId: Int? = nil
    var isBoardMenuExpanded: Bool = false
    var isVoteEnabled: Bool = false
    var voteTitle: String = ""
    var voteOptions: [VoteOptionModel] = [
        VoteOptionModel(id: 0, text: ""),
        VoteOptionModel(id: 1, text: "")
    ]
    var isSubmitting: Bool = false
    var isSubmitSuccess: Bool = false
    var submitError: String? = nil

    // Computed properties
    var isSubmitEnabled: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        selectedBoardId != nil &&
        isPollValid &&
        !isSubmitting
    }

    var hasContent: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
        !attachedImages.isEmpty
    }

    var selectedBoardName: String {
        boardOptions.first { $0.id == selectedBoardId }?.name ?? "게시판 선택"
    }

    private var isPollValid: Bool {
        if !isVoteEnabled { return true }
        let validTitle = !voteTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        let validOptionsCount = voteOptions.filter { !$0.text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }.count >= 2
        return validTitle && validOptionsCount
    }
}

// MARK: - ViewModel

@Observable
@MainActor
final class BoardWriteViewModel {
    private let postRepository: PostRepository
    private let boardRepository: BoardRepository
    private let uploadRepository: UploadRepository
    private let initialBoardId: Int?

    var uiState = BoardWriteUiState()

    init(
        postRepository: PostRepository,
        boardRepository: BoardRepository,
        uploadRepository: UploadRepository,
        initialBoardId: Int? = nil
    ) {
        self.postRepository = postRepository
        self.boardRepository = boardRepository
        self.uploadRepository = uploadRepository
        self.initialBoardId = initialBoardId

        // Pre-select board if provided
        if let boardId = initialBoardId {
            uiState.selectedBoardId = boardId
        }

        Task {
            await loadBoards()
        }
    }

    // MARK: - Title & Content

    func onTitleChange(_ value: String) {
        uiState.title = value
    }

    func onContentChange(_ value: String) {
        uiState.content = value
    }

    // MARK: - Board Selection

    func onToggleBoardMenu(_ expanded: Bool) {
        uiState.isBoardMenuExpanded = expanded
    }

    func onBoardSelected(_ boardId: Int) {
        uiState.selectedBoardId = boardId
        uiState.isBoardMenuExpanded = false
    }

    // MARK: - Image Attachment

    func onImageAttached(_ image: UIImage) {
        // 최대 5개까지 이미지 추가 가능
        guard uiState.attachedImages.count < 5 else { return }
        uiState.attachedImages.append(image)
    }

    var canAddMoreImages: Bool {
        uiState.attachedImages.count < 5
    }

    func onRemoveImage(_ image: UIImage) {
        uiState.attachedImages.removeAll { $0 === image }
    }

    func onRemoveImageAt(_ index: Int) {
        guard index >= 0 && index < uiState.attachedImages.count else { return }
        uiState.attachedImages.remove(at: index)
    }

    // MARK: - Vote

    func onToggleVote() {
        uiState.isVoteEnabled.toggle()
        if !uiState.isVoteEnabled {
            // Reset vote state when disabled
            uiState.voteTitle = ""
            uiState.voteOptions = [
                VoteOptionModel(id: 0, text: ""),
                VoteOptionModel(id: 1, text: "")
            ]
        }
    }

    func onVoteTitleChange(_ value: String) {
        uiState.voteTitle = value
    }

    func onVoteOptionChange(id: Int, text: String) {
        if let index = uiState.voteOptions.firstIndex(where: { $0.id == id }) {
            uiState.voteOptions[index].text = text
        }
    }

    func onAddVoteOption() {
        guard uiState.voteOptions.count < 5 else { return }
        let nextId = (uiState.voteOptions.map { $0.id }.max() ?? -1) + 1
        uiState.voteOptions.append(VoteOptionModel(id: nextId, text: ""))
    }

    func onRemoveVoteOption(id: Int) {
        guard uiState.voteOptions.count > 2 else { return }
        uiState.voteOptions.removeAll { $0.id == id }
    }

    // MARK: - Submit

    func onSubmit() async {
        guard uiState.isSubmitEnabled else { return }
        guard let selectedBoardId = uiState.selectedBoardId else {
            uiState.submitError = "게시판을 선택해주세요."
            return
        }

        uiState.isSubmitting = true
        uiState.submitError = nil

        // Upload images if attached
        var imageUrls: [String] = []
        for image in uiState.attachedImages {
            let uploadResult = await uploadImage(image)
            switch uploadResult {
            case .success(let url):
                imageUrls.append(url)
            case .failure(let error):
                uiState.isSubmitting = false
                uiState.submitError = "이미지 업로드에 실패했습니다. \(error.localizedDescription)"
                return
            }
        }

        // Build poll info if enabled
        let pollInfo = buildPollInfo()

        // Create post
        let postInfo = PostCreateInfo(
            title: uiState.title.trimmingCharacters(in: .whitespacesAndNewlines),
            content: uiState.content.trimmingCharacters(in: .whitespacesAndNewlines),
            boardId: selectedBoardId,
            poll: pollInfo,
            images: imageUrls
        )

        let result = await postRepository.createPost(post: postInfo)

        switch result {
        case .success:
            uiState.isSubmitSuccess = true
        case .failure(let error):
            uiState.submitError = "게시글 작성에 실패했습니다. \(error.localizedDescription)"
        }

        uiState.isSubmitting = false
    }

    func clearSubmitError() {
        uiState.submitError = nil
    }

    func clearSubmitSuccess() {
        uiState.isSubmitSuccess = false
    }

    // MARK: - Private Methods

    private func loadBoards() async {
        let result = await boardRepository.getBoards()

        switch result {
        case .success(let boards):
            uiState.boardOptions = boards.map { BoardOptionModel(id: $0.id, name: $0.title) }
            // Set default board if none selected (prefer initialBoardId, then first board)
            if uiState.selectedBoardId == nil {
                if let initialId = initialBoardId, boards.contains(where: { $0.id == initialId }) {
                    uiState.selectedBoardId = initialId
                } else {
                    uiState.selectedBoardId = uiState.boardOptions.first?.id
                }
            }
        case .failure(let error):
            print("Failed to load boards: \(error)")
            uiState.submitError = "게시판 목록을 불러오지 못했습니다."
        }
    }

    private func buildPollInfo() -> PollCreateInfo? {
        guard uiState.isVoteEnabled else { return nil }

        let title = uiState.voteTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        let options = uiState.voteOptions
            .map { $0.text.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        guard !title.isEmpty && options.count >= 2 else { return nil }

        return PollCreateInfo(title: title, options: options)
    }

    private func uploadImage(_ image: UIImage) async -> Result<String, Error> {
        // Compress and resize image
        guard let imageData = prepareImageData(image) else {
            return .failure(NSError(domain: "BoardWrite", code: -1, userInfo: [NSLocalizedDescriptionKey: "이미지 처리에 실패했습니다."]))
        }

        return await uploadRepository.uploadImage(image: imageData)
    }

    private func prepareImageData(_ image: UIImage) -> Data? {
        let maxDimension: CGFloat = 1280
        let maxBytes = 1_000_000 // ~1MB

        // Scale down if needed
        var processedImage = image
        let largestSide = max(image.size.width, image.size.height)
        if largestSide > maxDimension {
            let scale = maxDimension / largestSide
            let newSize = CGSize(
                width: image.size.width * scale,
                height: image.size.height * scale
            )
            UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            image.draw(in: CGRect(origin: .zero, size: newSize))
            if let scaledImage = UIGraphicsGetImageFromCurrentImageContext() {
                processedImage = scaledImage
            }
            UIGraphicsEndImageContext()
        }

        // Compress to fit size limit
        var quality: CGFloat = 0.9
        var imageData = processedImage.jpegData(compressionQuality: quality)

        while let data = imageData, data.count > maxBytes, quality > 0.3 {
            quality -= 0.1
            imageData = processedImage.jpegData(compressionQuality: quality)
        }

        return imageData
    }
}
