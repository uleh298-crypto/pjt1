import Foundation

final class CommentRepositoryImpl: CommentRepository {
    private let commentService: CommentService

    init(commentService: CommentService) {
        self.commentService = commentService
    }

    func like(commentId: Int) async -> Result<CommentLikeModel, Error> {
        do {
            let response = try await commentService.likeComment(commentId: commentId)
            return .success(response.toRepositoryModel())
        } catch {
            return .failure(error)
        }
    }

    func unlike(commentId: Int) async -> Result<CommentLikeModel, Error> {
        do {
            let response = try await commentService.unlikeComment(commentId: commentId)
            return .success(response.toRepositoryModel())
        } catch {
            return .failure(error)
        }
    }

    func updateComment(commentId: Int, content: String) async -> Result<Void, Error> {
        do {
            try await commentService.updateComment(commentId: commentId, content: content)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func deleteComment(commentId: Int) async -> Result<Void, Error> {
        do {
            try await commentService.deleteComment(commentId: commentId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

extension CommentLikeResponse {
    func toRepositoryModel() -> CommentLikeModel {
        CommentLikeModel(liked: liked, likeCount: likeCount)
    }
}
