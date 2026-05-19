import Foundation

struct PagedPostModel {
    let data: [PostModel]
    let hasNext: Bool
    let nextCursor: String?
}

struct PostModel {
    let id: Int
    let boardId: Int
    let boardName: String
    let isMine: Bool  // 본인 작성 여부 (서버에서 제공)
    let title: String
    let content: String
    let viewCount: Int
    let likeCount: Int
    let commentCount: Int
    let createdAt: String
    let imageUrls: [String]
    let isBlinded: Bool

    // Computed property for date text formatting (상대 시간: "지금", "N분 전", "N시간 전", "N일 전", "yyyy.MM.dd")
    var dateText: String {
        createdAt.toRelativeTimeText()
    }

    // Computed property for preview (truncated content)
    var preview: String {
        let cleanContent = content.replacingOccurrences(of: "\n", with: " ")
        if cleanContent.count > 100 {
            return String(cleanContent.prefix(100)) + "..."
        }
        return cleanContent
    }

    // Computed property for first image URL
    var imageUrl: String? {
        imageUrls.first
    }
}

struct PostDetailModel: Equatable {
    let createdAt: String
    let updatedAt: String
    let id: Int
    let boardId: Int
    let isMine: Bool  // 본인 작성 여부 (서버에서 제공)
    let title: String
    let content: String
    let isBlinded: Bool
    let imageUrls: [String]
    let poll: PollModel?
    let likeCount: Int
    let isLiked: Bool
    let commentCount: Int
    let scrapCount: Int
    let isScraped: Bool
    let comments: [CommentModel]

    // isAuthor는 isMine과 동일 (기존 코드 호환성)
    var isAuthor: Bool { isMine }

    // Computed property for date text formatting (올해면 "MM.dd HH:mm", 아니면 "yyyy.MM.dd HH:mm")
    var dateText: String {
        createdAt.toAdaptiveKstText()
    }
}

struct CommentModel: Equatable, Identifiable, Hashable {
    let id: Int
    let createdAt: String
    let content: String
    let likeCount: Int
    let isLiked: Bool
    let isBlinded: Bool
    let isDeleted: Bool
    let anon: AnonModel?
    let replies: [ReplyModel]

    // 현재 사용자가 작성한 댓글인지 여부 (anon.isMine에서 가져옴)
    var isMine: Bool {
        anon?.isMine ?? false
    }

    // 게시글 작성자인지 여부 (anon.isAuthor에서 가져옴)
    var isAuthor: Bool {
        anon?.isAuthor ?? false
    }

    // Computed property for nickname
    var nickname: String {
        anon?.name ?? "익명"
    }

    // Computed property for date text formatting (올해면 "MM.dd HH:mm", 아니면 "yyyy.MM.dd HH:mm")
    var dateText: String {
        createdAt.toAdaptiveKstText()
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(content)
        hasher.combine(likeCount)
        hasher.combine(isLiked)
        hasher.combine(isDeleted)
        hasher.combine(isBlinded)
        hasher.combine(replies)
    }
}

struct ReplyModel: Equatable, Identifiable, Hashable {
    let id: Int
    let createdAt: String
    let content: String
    let likeCount: Int
    let isLiked: Bool
    let isBlinded: Bool
    let isDeleted: Bool
    let anon: AnonModel?

    // 현재 사용자가 작성한 답글인지 여부 (anon.isMine에서 가져옴)
    var isMine: Bool {
        anon?.isMine ?? false
    }

    // 게시글 작성자인지 여부 (anon.isAuthor에서 가져옴)
    var isAuthor: Bool {
        anon?.isAuthor ?? false
    }

    // Computed property for nickname
    var nickname: String {
        anon?.name ?? "익명"
    }

    // Computed property for date text formatting (올해면 "MM.dd HH:mm", 아니면 "yyyy.MM.dd HH:mm")
    var dateText: String {
        createdAt.toAdaptiveKstText()
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(content)
        hasher.combine(likeCount)
        hasher.combine(isLiked)
        hasher.combine(isDeleted)
        hasher.combine(isBlinded)
    }
}

struct PollModel: Equatable {
    let pollId: Int
    let totalVotes: Int
    let myVotedOptionId: Int?
    let options: [PollOptionModel]

    // Computed property to check if user has voted
    var isVoted: Bool {
        myVotedOptionId != nil
    }
}

struct PollOptionModel: Equatable {
    let optionId: Int
    let text: String
    let voteCount: Int

    // Computed property to check if this option is voted
    func isVoted(myVotedOptionId: Int?) -> Bool {
        guard let votedId = myVotedOptionId else { return false }
        return optionId == votedId
    }
}

struct PostLikeModel {
    let liked: Bool
    let likeCount: Int
}

struct ScrapModel {
    let success: Bool
}

struct PostCreateInfo {
    let title: String
    let content: String
    let boardId: Int
    let poll: PollCreateInfo?
    let images: [String]
}

struct PollCreateInfo {
    let title: String
    let options: [String]
}

struct PostUpdateInfo {
    let title: String
    let content: String
}

struct CommentCreateInfo {
    let content: String
}

struct ReplyCreateInfo {
    let content: String
}

struct VoteInfo {
    let optionId: Int
}
