import Foundation
import Observation

// MARK: - UI State

struct BoardDetailUiState {
    var post: PostDetailModel? = nil
    var isLoading: Bool = false
    var error: String? = nil
    var commentText: String = ""
    var replyTargetComment: CommentModel? = nil
    var isCommentSubmitting: Bool = false
    var isLikeInProgress: Bool = false
    var isBookmarkInProgress: Bool = false
    var isVoteInProgress: Bool = false
    var isDeleting: Bool = false
    var isDeleteSuccess: Bool = false
    // 댓글 수정 관련
    var editingComment: CommentModel? = nil
    var editingReply: ReplyModel? = nil
    var editCommentText: String = ""
}

// MARK: - ViewModel

@Observable
@MainActor
final class BoardDetailViewModel {
    private let postRepository: PostRepository
    private let commentRepository: CommentRepository
    private let reportRepository: ReportRepository
    private(set) var uiState = BoardDetailUiState()
    private var currentPostId: Int?

    var commentText: String {
        get { uiState.commentText }
        set { uiState.commentText = newValue }
    }

    var editCommentText: String {
        get { uiState.editCommentText }
        set { uiState.editCommentText = newValue }
    }

    init(postRepository: PostRepository, commentRepository: CommentRepository, reportRepository: ReportRepository) {
        self.postRepository = postRepository
        self.commentRepository = commentRepository
        self.reportRepository = reportRepository
    }

    func loadPost(postId: Int) async {
        currentPostId = postId
        uiState.isLoading = true
        uiState.error = nil

        let result = await postRepository.getPostDetail(postId: postId)

        switch result {
        case .success(let detail):
            uiState.post = detail
        case .failure(let error):
            print("Error loading post: \(error)")
            uiState.error = "게시글을 불러오는 중 오류가 발생했습니다."
        }
        uiState.isLoading = false
    }

    // MARK: - Like/Bookmark

    func onLikePost() async {
        guard let postId = currentPostId, let post = uiState.post else { return }
        guard !uiState.isLikeInProgress else { return }

        uiState.isLikeInProgress = true

        let result = post.isLiked
            ? await postRepository.unlike(postId: postId)
            : await postRepository.like(postId: postId)

        switch result {
        case .success(let likeResult):
            if let currentPost = uiState.post {
                let updatedPost = PostDetailModel(
                    createdAt: currentPost.createdAt,
                    updatedAt: currentPost.updatedAt,
                    id: currentPost.id,
                    boardId: currentPost.boardId,
                    isMine: currentPost.isMine,
                    title: currentPost.title,
                    content: currentPost.content,
                    isBlinded: currentPost.isBlinded,
                    imageUrls: currentPost.imageUrls,
                    poll: currentPost.poll,
                    likeCount: likeResult.likeCount,
                    isLiked: likeResult.liked,
                    commentCount: currentPost.commentCount,
                    scrapCount: currentPost.scrapCount,
                    isScraped: currentPost.isScraped,
                    comments: currentPost.comments
                )
                uiState.post = updatedPost
            }
        case .failure(let error):
            print("Like failed: \(error)")
        }

        uiState.isLikeInProgress = false
    }

    func onBookmarkPost() async {
        guard let postId = currentPostId, let post = uiState.post else { return }
        guard !uiState.isBookmarkInProgress else { return }

        uiState.isBookmarkInProgress = true

        let result = post.isScraped
            ? await postRepository.unscrap(postId: postId)
            : await postRepository.scrap(postId: postId)

        switch result {
        case .success(let scrapResult):
            if let currentPost = uiState.post {
                let newScrapCount = scrapResult.success
                    ? (post.isScraped ? currentPost.scrapCount - 1 : currentPost.scrapCount + 1)
                    : currentPost.scrapCount
                let updatedPost = PostDetailModel(
                    createdAt: currentPost.createdAt,
                    updatedAt: currentPost.updatedAt,
                    id: currentPost.id,
                    boardId: currentPost.boardId,
                    isMine: currentPost.isMine,
                    title: currentPost.title,
                    content: currentPost.content,
                    isBlinded: currentPost.isBlinded,
                    imageUrls: currentPost.imageUrls,
                    poll: currentPost.poll,
                    likeCount: currentPost.likeCount,
                    isLiked: currentPost.isLiked,
                    commentCount: currentPost.commentCount,
                    scrapCount: newScrapCount,
                    isScraped: scrapResult.success ? !post.isScraped : post.isScraped,
                    comments: currentPost.comments
                )
                uiState.post = updatedPost
            }
        case .failure(let error):
            print("Bookmark failed: \(error)")
        }

        uiState.isBookmarkInProgress = false
    }

    // MARK: - Comments

    func onReplyComment(comment: CommentModel) {
        uiState.replyTargetComment = comment
    }

    func cancelReply() {
        uiState.replyTargetComment = nil
    }

    func onSubmitComment() async {
        guard let postId = currentPostId else { return }
        let text = uiState.commentText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !uiState.isCommentSubmitting else { return }

        uiState.isCommentSubmitting = true

        if let replyTarget = uiState.replyTargetComment {
            // Reply to comment
            let result = await postRepository.createReply(
                postId: postId,
                commentId: replyTarget.id,
                reply: ReplyCreateInfo(content: text)
            )

            switch result {
            case .success(let newReply):
                uiState.commentText = ""
                uiState.replyTargetComment = nil
                // 로컬 상태 업데이트 (전체 리로드하지 않음)
                if let currentPost = uiState.post {
                    let updatedComments = currentPost.comments.map { comment in
                        if comment.id == replyTarget.id {
                            // 해당 댓글에 새 답글 추가
                            return CommentModel(
                                id: comment.id,
                                createdAt: comment.createdAt,
                                content: comment.content,
                                likeCount: comment.likeCount,
                                isLiked: comment.isLiked,
                                isBlinded: comment.isBlinded,
                                isDeleted: comment.isDeleted,
                                anon: comment.anon,
                                replies: comment.replies + [newReply]
                            )
                        } else {
                            return comment
                        }
                    }
                    uiState.post = PostDetailModel(
                        createdAt: currentPost.createdAt,
                        updatedAt: currentPost.updatedAt,
                        id: currentPost.id,
                        boardId: currentPost.boardId,
                        isMine: currentPost.isMine,
                        title: currentPost.title,
                        content: currentPost.content,
                        isBlinded: currentPost.isBlinded,
                        imageUrls: currentPost.imageUrls,
                        poll: currentPost.poll,
                        likeCount: currentPost.likeCount,
                        isLiked: currentPost.isLiked,
                        commentCount: currentPost.commentCount + 1,
                        scrapCount: currentPost.scrapCount,
                        isScraped: currentPost.isScraped,
                        comments: updatedComments
                    )
                }
            case .failure(let error):
                print("Reply failed: \(error)")
            }
        } else {
            // Create new comment
            let result = await postRepository.createComment(
                postId: postId,
                comment: CommentCreateInfo(content: text)
            )

            switch result {
            case .success(let newComment):
                uiState.commentText = ""
                // 로컬 상태 업데이트 (전체 리로드하지 않음)
                if let currentPost = uiState.post {
                    uiState.post = PostDetailModel(
                        createdAt: currentPost.createdAt,
                        updatedAt: currentPost.updatedAt,
                        id: currentPost.id,
                        boardId: currentPost.boardId,
                        isMine: currentPost.isMine,
                        title: currentPost.title,
                        content: currentPost.content,
                        isBlinded: currentPost.isBlinded,
                        imageUrls: currentPost.imageUrls,
                        poll: currentPost.poll,
                        likeCount: currentPost.likeCount,
                        isLiked: currentPost.isLiked,
                        commentCount: currentPost.commentCount + 1,
                        scrapCount: currentPost.scrapCount,
                        isScraped: currentPost.isScraped,
                        comments: currentPost.comments + [newComment]
                    )
                }
            case .failure(let error):
                print("Comment failed: \(error)")
            }
        }

        uiState.isCommentSubmitting = false
    }

    // MARK: - Vote

    func onVote(optionId: Int) async {
        guard let postId = currentPostId else { return }
        guard !uiState.isVoteInProgress else { return }

        uiState.isVoteInProgress = true

        let result = await postRepository.vote(postId: postId, vote: VoteInfo(optionId: optionId))

        switch result {
        case .success(let poll):
            if let currentPost = uiState.post {
                let updatedPost = PostDetailModel(
                    createdAt: currentPost.createdAt,
                    updatedAt: currentPost.updatedAt,
                    id: currentPost.id,
                    boardId: currentPost.boardId,
                    isMine: currentPost.isMine,
                    title: currentPost.title,
                    content: currentPost.content,
                    isBlinded: currentPost.isBlinded,
                    imageUrls: currentPost.imageUrls,
                    poll: poll,
                    likeCount: currentPost.likeCount,
                    isLiked: currentPost.isLiked,
                    commentCount: currentPost.commentCount,
                    scrapCount: currentPost.scrapCount,
                    isScraped: currentPost.isScraped,
                    comments: currentPost.comments
                )
                uiState.post = updatedPost
            }
        case .failure(let error):
            print("Vote failed: \(error)")
        }

        uiState.isVoteInProgress = false
    }

    // MARK: - Post Delete

    func deletePost() async {
        guard let postId = currentPostId else { return }
        guard !uiState.isDeleting else { return }

        uiState.isDeleting = true

        let result = await postRepository.deletePost(postId: postId)

        switch result {
        case .success:
            uiState.isDeleteSuccess = true
        case .failure(let error):
            print("Delete post failed: \(error)")
            uiState.error = "게시글 삭제에 실패했습니다."
        }

        uiState.isDeleting = false
    }

    // MARK: - Comment Like

    func onLikeComment(comment: CommentModel) async {
        let result = comment.isLiked
            ? await commentRepository.unlike(commentId: comment.id)
            : await commentRepository.like(commentId: comment.id)

        switch result {
        case .success(let likeResult):
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                if c.id == comment.id {
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: c.content,
                        likeCount: likeResult.likeCount,
                        isLiked: likeResult.liked,
                        isBlinded: c.isBlinded,
                        isDeleted: c.isDeleted,
                        anon: c.anon,
                        replies: c.replies
                    )
                } else {
                    // 대댓글 내에서 찾기
                    let updatedReplies = c.replies.map { r in
                        if r.id == comment.id {
                            return ReplyModel(
                                id: r.id,
                                createdAt: r.createdAt,
                                content: r.content,
                                likeCount: likeResult.likeCount,
                                isLiked: likeResult.liked,
                                isBlinded: r.isBlinded,
                                isDeleted: r.isDeleted,
                                anon: r.anon
                            )
                        } else {
                            return r
                        }
                    }
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: c.content,
                        likeCount: c.likeCount,
                        isLiked: c.isLiked,
                        isBlinded: c.isBlinded,
                        isDeleted: c.isDeleted,
                        anon: c.anon,
                        replies: updatedReplies
                    )
                }
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Comment like failed: \(error)")
        }
    }

    func onLikeReply(reply: ReplyModel, parentComment: CommentModel) async {
        let result = reply.isLiked
            ? await commentRepository.unlike(commentId: reply.id)
            : await commentRepository.like(commentId: reply.id)

        switch result {
        case .success(let likeResult):
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                if c.id == parentComment.id {
                    let updatedReplies = c.replies.map { r in
                        if r.id == reply.id {
                            return ReplyModel(
                                id: r.id,
                                createdAt: r.createdAt,
                                content: r.content,
                                likeCount: likeResult.likeCount,
                                isLiked: likeResult.liked,
                                isBlinded: r.isBlinded,
                                isDeleted: r.isDeleted,
                                anon: r.anon
                            )
                        } else {
                            return r
                        }
                    }
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: c.content,
                        likeCount: c.likeCount,
                        isLiked: c.isLiked,
                        isBlinded: c.isBlinded,
                        isDeleted: c.isDeleted,
                        anon: c.anon,
                        replies: updatedReplies
                    )
                } else {
                    return c
                }
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Reply like failed: \(error)")
        }
    }

    // MARK: - Comment Edit

    func startEditComment(comment: CommentModel) {
        uiState.editingComment = comment
        uiState.editingReply = nil
        uiState.editCommentText = comment.content
    }

    func startEditReply(reply: ReplyModel) {
        uiState.editingComment = nil
        uiState.editingReply = reply
        uiState.editCommentText = reply.content
    }

    func cancelEditComment() {
        uiState.editingComment = nil
        uiState.editingReply = nil
        uiState.editCommentText = ""
    }

    func submitEditComment() async {
        // alert이 닫히면서 cancelEditComment()가 호출되기 전에 값을 캡처
        let newContent = uiState.editCommentText.trimmingCharacters(in: .whitespacesAndNewlines)
        let editingComment = uiState.editingComment
        let editingReply = uiState.editingReply

        guard !newContent.isEmpty else { return }

        if let editingComment = editingComment {
            let result = await commentRepository.updateComment(commentId: editingComment.id, content: newContent)

            switch result {
            case .success:
                guard let currentPost = uiState.post else { return }
                let updatedComments = currentPost.comments.map { c in
                    if c.id == editingComment.id {
                        return CommentModel(
                            id: c.id,
                            createdAt: c.createdAt,
                            content: newContent,
                            likeCount: c.likeCount,
                            isLiked: c.isLiked,
                            isBlinded: c.isBlinded,
                            isDeleted: c.isDeleted,
                            anon: c.anon,
                            replies: c.replies
                        )
                    } else {
                        return c
                    }
                }
                uiState.post = PostDetailModel(
                    createdAt: currentPost.createdAt,
                    updatedAt: currentPost.updatedAt,
                    id: currentPost.id,
                    boardId: currentPost.boardId,
                    isMine: currentPost.isMine,
                    title: currentPost.title,
                    content: currentPost.content,
                    isBlinded: currentPost.isBlinded,
                    imageUrls: currentPost.imageUrls,
                    poll: currentPost.poll,
                    likeCount: currentPost.likeCount,
                    isLiked: currentPost.isLiked,
                    commentCount: currentPost.commentCount,
                    scrapCount: currentPost.scrapCount,
                    isScraped: currentPost.isScraped,
                    comments: updatedComments
                )
            case .failure(let error):
                print("Edit comment failed: \(error)")
                uiState.error = "댓글 수정에 실패했습니다."
            }
        } else if let editingReply = editingReply {
            let result = await commentRepository.updateComment(commentId: editingReply.id, content: newContent)

            switch result {
            case .success:
                guard let currentPost = uiState.post else { return }
                let updatedComments = currentPost.comments.map { c in
                    let updatedReplies = c.replies.map { r in
                        if r.id == editingReply.id {
                            return ReplyModel(
                                id: r.id,
                                createdAt: r.createdAt,
                                content: newContent,
                                likeCount: r.likeCount,
                                isLiked: r.isLiked,
                                isBlinded: r.isBlinded,
                                isDeleted: r.isDeleted,
                                anon: r.anon
                            )
                        } else {
                            return r
                        }
                    }
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: c.content,
                        likeCount: c.likeCount,
                        isLiked: c.isLiked,
                        isBlinded: c.isBlinded,
                        isDeleted: c.isDeleted,
                        anon: c.anon,
                        replies: updatedReplies
                    )
                }
                uiState.post = PostDetailModel(
                    createdAt: currentPost.createdAt,
                    updatedAt: currentPost.updatedAt,
                    id: currentPost.id,
                    boardId: currentPost.boardId,
                    isMine: currentPost.isMine,
                    title: currentPost.title,
                    content: currentPost.content,
                    isBlinded: currentPost.isBlinded,
                    imageUrls: currentPost.imageUrls,
                    poll: currentPost.poll,
                    likeCount: currentPost.likeCount,
                    isLiked: currentPost.isLiked,
                    commentCount: currentPost.commentCount,
                    scrapCount: currentPost.scrapCount,
                    isScraped: currentPost.isScraped,
                    comments: updatedComments
                )
            case .failure(let error):
                print("Edit reply failed: \(error)")
                uiState.error = "답글 수정에 실패했습니다."
            }
        }
    }

    // MARK: - Comment Update (직접 ID와 content를 받는 버전)

    func updateCommentContent(commentId: Int, content: String) async {
        let newContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !newContent.isEmpty else { return }

        let result = await commentRepository.updateComment(commentId: commentId, content: newContent)

        switch result {
        case .success:
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                if c.id == commentId {
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: newContent,
                        likeCount: c.likeCount,
                        isLiked: c.isLiked,
                        isBlinded: c.isBlinded,
                        isDeleted: c.isDeleted,
                        anon: c.anon,
                        replies: c.replies
                    )
                } else {
                    return c
                }
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Edit comment failed: \(error)")
            uiState.error = "댓글 수정에 실패했습니다."
        }
    }

    func updateReplyContent(replyId: Int, content: String) async {
        let newContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !newContent.isEmpty else { return }

        let result = await commentRepository.updateComment(commentId: replyId, content: newContent)

        switch result {
        case .success:
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                let updatedReplies = c.replies.map { r in
                    if r.id == replyId {
                        return ReplyModel(
                            id: r.id,
                            createdAt: r.createdAt,
                            content: newContent,
                            likeCount: r.likeCount,
                            isLiked: r.isLiked,
                            isBlinded: r.isBlinded,
                            isDeleted: r.isDeleted,
                            anon: r.anon
                        )
                    } else {
                        return r
                    }
                }
                return CommentModel(
                    id: c.id,
                    createdAt: c.createdAt,
                    content: c.content,
                    likeCount: c.likeCount,
                    isLiked: c.isLiked,
                    isBlinded: c.isBlinded,
                    isDeleted: c.isDeleted,
                    anon: c.anon,
                    replies: updatedReplies
                )
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Edit reply failed: \(error)")
            uiState.error = "답글 수정에 실패했습니다."
        }
    }

    // MARK: - Comment Delete

    func deleteComment(comment: CommentModel) async {
        let result = await commentRepository.deleteComment(commentId: comment.id)

        switch result {
        case .success:
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                if c.id == comment.id {
                    return CommentModel(
                        id: c.id,
                        createdAt: c.createdAt,
                        content: "삭제된 댓글입니다.",
                        likeCount: c.likeCount,
                        isLiked: c.isLiked,
                        isBlinded: c.isBlinded,
                        isDeleted: true,
                        anon: c.anon,
                        replies: c.replies
                    )
                } else {
                    return c
                }
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Delete comment failed: \(error)")
            uiState.error = "댓글 삭제에 실패했습니다."
        }
    }

    func deleteReply(reply: ReplyModel) async {
        let result = await commentRepository.deleteComment(commentId: reply.id)

        switch result {
        case .success:
            guard let currentPost = uiState.post else { return }
            let updatedComments = currentPost.comments.map { c in
                let updatedReplies = c.replies.map { r in
                    if r.id == reply.id {
                        return ReplyModel(
                            id: r.id,
                            createdAt: r.createdAt,
                            content: "삭제된 답글입니다.",
                            likeCount: r.likeCount,
                            isLiked: r.isLiked,
                            isBlinded: r.isBlinded,
                            isDeleted: true,
                            anon: r.anon
                        )
                    } else {
                        return r
                    }
                }
                return CommentModel(
                    id: c.id,
                    createdAt: c.createdAt,
                    content: c.content,
                    likeCount: c.likeCount,
                    isLiked: c.isLiked,
                    isBlinded: c.isBlinded,
                    isDeleted: c.isDeleted,
                    anon: c.anon,
                    replies: updatedReplies
                )
            }
            uiState.post = PostDetailModel(
                createdAt: currentPost.createdAt,
                updatedAt: currentPost.updatedAt,
                id: currentPost.id,
                boardId: currentPost.boardId,
                isMine: currentPost.isMine,
                title: currentPost.title,
                content: currentPost.content,
                isBlinded: currentPost.isBlinded,
                imageUrls: currentPost.imageUrls,
                poll: currentPost.poll,
                likeCount: currentPost.likeCount,
                isLiked: currentPost.isLiked,
                commentCount: currentPost.commentCount,
                scrapCount: currentPost.scrapCount,
                isScraped: currentPost.isScraped,
                comments: updatedComments
            )
        case .failure(let error):
            print("Delete reply failed: \(error)")
            uiState.error = "답글 삭제에 실패했습니다."
        }
    }

    // MARK: - Report

    func reportPost(reason: ReportReason, detail: String?) async {
        guard let postId = currentPostId else {
            print("[Report] No currentPostId found")
            return
        }

        print("[Report] Reporting post: postId=\(postId), reason=\(reason.rawValue), detail=\(detail ?? "nil")")

        let result = await reportRepository.createReport(
            targetType: .post,
            targetId: postId,
            reason: reason,
            detail: detail
        )

        switch result {
        case .success:
            print("[Report] Post reported successfully")
        case .failure(let error):
            print("[Report] Report post failed: \(error)")
            uiState.error = "신고에 실패했습니다."
        }
    }

    func reportComment(commentId: Int, reason: ReportReason, detail: String?) async {
        print("[Report] Reporting comment: commentId=\(commentId), reason=\(reason.rawValue), detail=\(detail ?? "nil")")

        let result = await reportRepository.createReport(
            targetType: .comment,
            targetId: commentId,
            reason: reason,
            detail: detail
        )

        switch result {
        case .success:
            print("[Report] Comment reported successfully")
        case .failure(let error):
            print("[Report] Report comment failed: \(error)")
            uiState.error = "신고에 실패했습니다."
        }
    }
}
