import Foundation

// MARK: - MyPage Response

struct MyPageResponse: Decodable {
    let user: MyPageUserInfoResponse?
    let counts: MyPageCountsResponse?
    let portfolioSummary: MyPagePortfolioSummaryResponse?
}

struct MyPageUserInfoResponse: Decodable {
    let userId: Int
    let name: String
    let mattermostId: String?
    let campus: String?
    let generation: Int?
    let profileImageUrl: String?
}

struct MyPageCountsResponse: Decodable {
    let postCount: Int
    let commentCount: Int
    let scrapCount: Int
}

struct MyPagePortfolioSummaryResponse: Decodable {
    let techStack: [String: String]?
    let ssafySwRating: String?
    let solvedAcRank: String?
    let solvedAcHandle: String?
    let solvedAcTierName: String?
    let solvedAcTierImageUrl: String?
    let solvedAcSolvedCount: Int?
    let links: [String]?
    let projects: [String]?
}

// MARK: - My Comment Response

struct MyCommentResponse: Decodable {
    let id: Int
    let content: String
    let createdAt: String?
    let isReply: Bool
    let postId: Int
    let postTitle: String
    let boardId: Int
    let boardName: String
}

// MARK: - Update Profile Request

struct UpdateProfileRequest: Encodable {
    let profileImageUrl: String?
}

struct UpdateProfileResponse: Decodable {
    let id: Int
    let email: String?
    let name: String?
    let studentNo: Int?
    let campus: String?
    let generation: Int?
    let classNo: Int?
    let mattermostId: String?
    let profileImageUrl: String?
    let deletedAt: String?
    let createdAt: String?
    let updatedAt: String?
}

// MARK: - Anon Response (기존)

struct AnonResponse: Codable {
    let name: String
    let isAuthor: Bool
    let isMine: Bool
}
