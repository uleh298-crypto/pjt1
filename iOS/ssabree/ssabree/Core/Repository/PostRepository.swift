import Foundation

protocol PostRepository {
    func getPosts(boardId: Int?, keyword: String?, cursor: String?, limit: Int) async -> Result<PagedPostModel, Error>
    func getHotPosts(cursor: String?, limit: Int) async -> Result<PagedPostModel, Error>
    func getPostDetail(postId: Int) async -> Result<PostDetailModel, Error>
    func createPost(post: PostCreateInfo) async -> Result<PostModel, Error>
    func updatePost(postId: Int, post: PostUpdateInfo) async -> Result<PostModel, Error>
    func deletePost(postId: Int) async -> Result<Void, Error>

    func like(postId: Int) async -> Result<PostLikeModel, Error>
    func unlike(postId: Int) async -> Result<PostLikeModel, Error>
    func scrap(postId: Int) async -> Result<ScrapModel, Error>
    func unscrap(postId: Int) async -> Result<ScrapModel, Error>

    func createComment(postId: Int, comment: CommentCreateInfo) async -> Result<CommentModel, Error>
    func createReply(postId: Int, commentId: Int, reply: ReplyCreateInfo) async -> Result<ReplyModel, Error>

    func vote(postId: Int, vote: VoteInfo) async -> Result<PollModel, Error>
}
