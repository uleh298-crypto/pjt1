import Foundation

protocol CommentRepository {
    func like(commentId: Int) async -> Result<CommentLikeModel, Error>
    func unlike(commentId: Int) async -> Result<CommentLikeModel, Error>
    func updateComment(commentId: Int, content: String) async -> Result<Void, Error>
    func deleteComment(commentId: Int) async -> Result<Void, Error>
}
