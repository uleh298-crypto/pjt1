import Foundation

// MARK: - MyPage Domain Models

struct MyPageModel {
    let user: MyPageUserModel?
    let counts: MyPageCountsModel?
    let portfolioSummary: MyPagePortfolioSummaryModel?
}

struct MyPageUserModel {
    let userId: Int
    let name: String
    let mattermostId: String?
    let campus: String?
    let generation: Int?
    let profileImageUrl: String?

    /// 표시용 MattermostId (@아이디)
    var displayMattermostId: String {
        if let id = mattermostId, !id.isEmpty {
            return "@\(id)"
        }
        return "@-"
    }

    /// 표시용 캠퍼스 정보
    var displayCampusInfo: String {
        switch (generation, campus?.isEmpty == false) {
        case (let gen?, true):
            return "\(gen)기 \(campus!) 캠퍼스"
        case (let gen?, false):
            return "\(gen)기"
        case (nil, true):
            return campus ?? "-"
        default:
            return "캠퍼스 정보 없음"
        }
    }
}

struct MyPageCountsModel {
    let postCount: Int
    let commentCount: Int
    let scrapCount: Int
}

struct MyPagePortfolioSummaryModel {
    let techStack: [String: String]
    let ssafySwRating: String?
    let solvedAcRank: String?
    let solvedAcHandle: String?
    let solvedAcTierName: String?
    let solvedAcTierImageUrl: String?
    let solvedAcSolvedCount: Int?
    let links: [String]
    let projects: [String]

    /// 마스킹된 SolvedAc 핸들 (앞 2자만 표시)
    var maskedSolvedAcHandle: String {
        guard let handle = solvedAcHandle?.trimmingCharacters(in: .whitespaces),
              !handle.isEmpty, handle != "-" else {
            return solvedAcHandle ?? "-"
        }
        if handle.count <= 2 {
            return String(repeating: "*", count: handle.count)
        }
        return String(handle.prefix(2)) + String(repeating: "*", count: handle.count - 2)
    }
}

struct MyCommentModel: Identifiable {
    let id: Int
    let content: String
    let createdAt: String?
    let isReply: Bool
    let postId: Int
    let postTitle: String
    let boardId: Int
    let boardName: String

    var dateText: String {
        createdAt?.toRelativeTimeText() ?? ""
    }
}

// MARK: - Response to Model Conversion

extension MyPageResponse {
    func toModel() -> MyPageModel {
        MyPageModel(
            user: user?.toModel(),
            counts: counts?.toModel(),
            portfolioSummary: portfolioSummary?.toModel()
        )
    }
}

extension MyPageUserInfoResponse {
    func toModel() -> MyPageUserModel {
        MyPageUserModel(
            userId: userId,
            name: name,
            mattermostId: mattermostId,
            campus: campus,
            generation: generation,
            profileImageUrl: profileImageUrl
        )
    }
}

extension MyPageCountsResponse {
    func toModel() -> MyPageCountsModel {
        MyPageCountsModel(
            postCount: postCount,
            commentCount: commentCount,
            scrapCount: scrapCount
        )
    }
}

extension MyPagePortfolioSummaryResponse {
    func toModel() -> MyPagePortfolioSummaryModel {
        MyPagePortfolioSummaryModel(
            techStack: techStack ?? [:],
            ssafySwRating: ssafySwRating,
            solvedAcRank: solvedAcRank,
            solvedAcHandle: solvedAcHandle,
            solvedAcTierName: solvedAcTierName,
            solvedAcTierImageUrl: solvedAcTierImageUrl,
            solvedAcSolvedCount: solvedAcSolvedCount,
            links: links ?? [],
            projects: projects ?? []
        )
    }
}

extension MyCommentResponse {
    func toModel() -> MyCommentModel {
        MyCommentModel(
            id: id,
            content: content,
            createdAt: createdAt,
            isReply: isReply,
            postId: postId,
            postTitle: postTitle,
            boardId: boardId,
            boardName: boardName
        )
    }
}
