import Foundation

final class PostRepositoryImpl: PostRepository {
    private let postService: PostService
    private let authDataStore: AuthDataStore

    init(postService: PostService, authDataStore: AuthDataStore) {
        self.postService = postService
        self.authDataStore = authDataStore
    }

    func getPosts(boardId: Int?, keyword: String?, cursor: String?, limit: Int = 20) async -> Result<PagedPostModel, Error> {
        do {
            let response = try await postService.getPosts(boardId: boardId, keyword: keyword, cursor: cursor, limit: limit)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func getHotPosts(cursor: String?, limit: Int = 20) async -> Result<PagedPostModel, Error> {
        do {
            let response = try await postService.getHotPosts(cursor: cursor, limit: limit)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func getPostDetail(postId: Int) async -> Result<PostDetailModel, Error> {
        do {
            let response = try await postService.getPost(id: postId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func createPost(post: PostCreateInfo) async -> Result<PostModel, Error> {
        do {
            let request = PostCreateRequest(
                title: post.title,
                content: post.content,
                boardId: post.boardId,
                imageUrls: post.images,
                poll: post.poll?.toRequest()
            )
            let response = try await postService.createPost(request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func updatePost(postId: Int, post: PostUpdateInfo) async -> Result<PostModel, Error> {
        do {
            let request = PostUpdateRequest(title: post.title, content: post.content)
            let response = try await postService.updatePost(id: postId, request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func deletePost(postId: Int) async -> Result<Void, Error> {
        do {
            try await postService.deletePost(id: postId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func like(postId: Int) async -> Result<PostLikeModel, Error> {
        do {
            let response = try await postService.likePost(postId: postId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func unlike(postId: Int) async -> Result<PostLikeModel, Error> {
        do {
            let response = try await postService.unlikePost(postId: postId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func scrap(postId: Int) async -> Result<ScrapModel, Error> {
        do {
            let response = try await postService.scrapPost(postId: postId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func unscrap(postId: Int) async -> Result<ScrapModel, Error> {
        do {
            let response = try await postService.unscrapPost(postId: postId)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func createComment(postId: Int, comment: CommentCreateInfo) async -> Result<CommentModel, Error> {
        do {
            let request = CommentCreateRequest(content: comment.content)
            let response = try await postService.createComment(postId: postId, request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func createReply(postId: Int, commentId: Int, reply: ReplyCreateInfo) async -> Result<ReplyModel, Error> {
        do {
            let request = ReplyCreateRequest(content: reply.content)
            let response = try await postService.createReply(postId: postId, commentId: commentId, request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func vote(postId: Int, vote: VoteInfo) async -> Result<PollModel, Error> {
        do {
            let request = VoteRequest(optionId: vote.optionId)
            let response = try await postService.vote(postId: postId, request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Response to Model Mappers

extension PagedPostResponse {
    func toModel() -> PagedPostModel {
        PagedPostModel(
            data: posts.map { $0.toModel() },
            hasNext: hasNext,
            nextCursor: nextCursor
        )
    }
}

extension PostResponse {
    func toModel() -> PostModel {
        PostModel(
            id: id,
            boardId: boardId,
            boardName: boardName,
            isMine: isMine,
            title: title,
            content: content,
            viewCount: viewCount,
            likeCount: likeCount,
            commentCount: commentCount,
            createdAt: createdAt ?? "",
            imageUrls: imageUrls ?? [],
            isBlinded: isBlinded
        )
    }
}

extension PostDetailResponse {
    func toModel() -> PostDetailModel {
        PostDetailModel(
            createdAt: createdAt ?? "",
            updatedAt: updatedAt ?? "",
            id: id,
            boardId: boardId,
            isMine: isMine,
            title: title,
            content: content,
            isBlinded: isBlinded,
            imageUrls: imageUrls ?? [],
            poll: poll?.toModel(),
            likeCount: likeCount,
            isLiked: isLiked,
            commentCount: commentCount,
            scrapCount: scrapCount,
            isScraped: isScraped,
            comments: comments.map { $0.toModel() }
        )
    }
}

extension CommentResponse {
    func toModel() -> CommentModel {
        CommentModel(
            id: id,
            createdAt: createdAt ?? "",
            content: content,
            likeCount: likeCount,
            isLiked: isLiked,
            isBlinded: isBlinded,
            isDeleted: isDeleted,
            anon: anon?.toModel(),
            replies: replies.map { $0.toModel() }
        )
    }
}

extension ReplyResponse {
    func toModel() -> ReplyModel {
        ReplyModel(
            id: id,
            createdAt: createdAt ?? "",
            content: content,
            likeCount: likeCount,
            isLiked: isLiked,
            isBlinded: isBlinded,
            isDeleted: isDeleted,
            anon: anon?.toModel()
        )
    }
}

extension PollResponse {
    func toModel() -> PollModel {
        PollModel(
            pollId: pollId,
            totalVotes: totalVotes,
            myVotedOptionId: myVotedOptionId,
            options: options.map { $0.toModel() }
        )
    }
}

extension PollOptionResponse {
    func toModel() -> PollOptionModel {
        PollOptionModel(
            optionId: optionId,
            text: text,
            voteCount: voteCount
        )
    }
}

extension PostLikeResponse {
    func toModel() -> PostLikeModel {
        PostLikeModel(
            liked: liked,
            likeCount: likeCount
        )
    }
}

extension ScrapResponse {
    func toModel() -> ScrapModel {
        ScrapModel(
            success: success
        )
    }
}

// MARK: - Info to Request Mappers

extension PollCreateInfo {
    func toRequest() -> PollCreateRequest {
        PollCreateRequest(
            title: title,
            options: options
        )
    }
}

// MARK: - Fake Repository

final class FakePostRepository: PostRepository {
    func getPosts(boardId: Int?, keyword: String?, cursor: String?, limit: Int) async -> Result<PagedPostModel, Error> {
        .success(PagedPostModel(data: [], hasNext: false, nextCursor: nil))
    }

    func getHotPosts(cursor: String?, limit: Int) async -> Result<PagedPostModel, Error> {
        .success(PagedPostModel(data: [], hasNext: false, nextCursor: nil))
    }

    func getPostDetail(postId: Int) async -> Result<PostDetailModel, Error> {
        .success(PostDetailModel(
            createdAt: "",
            updatedAt: "",
            id: 0,
            boardId: 0,
            isMine: false,
            title: "",
            content: "",
            isBlinded: false,
            imageUrls: [],
            poll: nil,
            likeCount: 0,
            isLiked: false,
            commentCount: 0,
            scrapCount: 0,
            isScraped: false,
            comments: []
        ))
    }

    func createPost(post: PostCreateInfo) async -> Result<PostModel, Error> {
        .success(PostModel(id: 0, boardId: 0, boardName: "", isMine: true, title: "", content: "", viewCount: 0, likeCount: 0, commentCount: 0, createdAt: "", imageUrls: [], isBlinded: false))
    }

    func updatePost(postId: Int, post: PostUpdateInfo) async -> Result<PostModel, Error> {
        .success(PostModel(id: 0, boardId: 0, boardName: "", isMine: true, title: "", content: "", viewCount: 0, likeCount: 0, commentCount: 0, createdAt: "", imageUrls: [], isBlinded: false))
    }

    func deletePost(postId: Int) async -> Result<Void, Error> {
        .success(())
    }

    func like(postId: Int) async -> Result<PostLikeModel, Error> {
        .success(PostLikeModel(liked: true, likeCount: 0))
    }

    func unlike(postId: Int) async -> Result<PostLikeModel, Error> {
        .success(PostLikeModel(liked: false, likeCount: 0))
    }

    func scrap(postId: Int) async -> Result<ScrapModel, Error> {
        .success(ScrapModel(success: true))
    }

    func unscrap(postId: Int) async -> Result<ScrapModel, Error> {
        .success(ScrapModel(success: true))
    }

    func createComment(postId: Int, comment: CommentCreateInfo) async -> Result<CommentModel, Error> {
        .success(CommentModel(id: 0, createdAt: "", content: "", likeCount: 0, isLiked: false, isBlinded: false, isDeleted: false, anon: AnonModel(name: "익명", isAuthor: false, isMine: true), replies: []))
    }

    func createReply(postId: Int, commentId: Int, reply: ReplyCreateInfo) async -> Result<ReplyModel, Error> {
        .success(ReplyModel(id: 0, createdAt: "", content: "", likeCount: 0, isLiked: false, isBlinded: false, isDeleted: false, anon: AnonModel(name: "익명", isAuthor: false, isMine: true)))
    }

    func vote(postId: Int, vote: VoteInfo) async -> Result<PollModel, Error> {
        .success(PollModel(pollId: 0, totalVotes: 0, myVotedOptionId: nil, options: []))
    }
}
