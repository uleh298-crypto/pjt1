import Foundation

// MARK: - Paged Post Response (게시글 목록)

struct PagedPostResponse: Codable {
    let posts: [PostResponse]
    let hasNext: Bool
    let nextCursor: String?
}

// MARK: - Post Response (게시글 목록 항목)

struct PostResponse: Codable {
    let id: Int
    let boardId: Int
    let boardName: String
    let isMine: Bool  // 본인 작성 여부 (memberId 대체)
    let title: String
    let content: String
    let viewCount: Int
    let likeCount: Int
    let commentCount: Int
    let createdAt: String?
    let updatedAt: String?
    let imageUrls: [String]?
    let isBlinded: Bool
}

// MARK: - Post Detail Response (게시글 상세)

struct PostDetailResponse: Codable {
    let createdAt: String?
    let updatedAt: String?
    let id: Int
    let boardId: Int
    let isMine: Bool  // 본인 작성 여부 (memberId 대체)
    let title: String
    let content: String
    let isBlinded: Bool
    let imageUrls: [String]?
    let poll: PollResponse?
    let likeCount: Int
    let isLiked: Bool
    let commentCount: Int
    let scrapCount: Int
    let isScraped: Bool
    let comments: [CommentResponse]
}

// MARK: - Comment Response (댓글)

struct CommentResponse: Codable {
    let id: Int
    let createdAt: String?
    let content: String
    let likeCount: Int
    let isLiked: Bool
    let isBlinded: Bool
    let isDeleted: Bool
    let anon: AnonResponse?  // isMine, isAuthor 포함
    let replies: [ReplyResponse]
}

// MARK: - Reply Response (대댓글)

struct ReplyResponse: Codable {
    let id: Int
    let createdAt: String?
    let content: String
    let likeCount: Int
    let isLiked: Bool
    let isBlinded: Bool
    let isDeleted: Bool
    let anon: AnonResponse?  // isMine, isAuthor 포함
}

// MARK: - Poll Response (투표)

struct PollResponse: Codable {
    let pollId: Int
    let totalVotes: Int
    let myVotedOptionId: Int?
    let options: [PollOptionResponse]
}

// MARK: - Poll Option Response (투표 옵션)

struct PollOptionResponse: Codable {
    let optionId: Int
    let text: String
    let voteCount: Int
}

// MARK: - Post Like Response (게시글 좋아요)

struct PostLikeResponse: Codable {
    let liked: Bool
    let likeCount: Int
}

// MARK: - Scrap Response (스크랩)

struct ScrapResponse: Codable {
    let success: Bool
}

// MARK: - Comment Like Response (댓글 좋아요)

struct CommentLikeResponse: Codable {
    let liked: Bool
    let likeCount: Int
}
