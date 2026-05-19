import Foundation

struct GroupResponse: Codable {
    let id: Int
    let title: String
    let content: String
    let category: String
    let maxMembers: Int
    let currentMembers: Int
    let dDay: String?
    let status: String
}

struct GroupListResponse: Codable {
    let content: [GroupResponse]
    let hasNext: Bool
}

struct AnnouncementResponse: Codable {
    let id: Int
    let title: String
    let content: String
    let createdAt: String
}

struct AnnouncementCreateRequest: Codable {
    let title: String
    let content: String
}

struct AnnouncementUpdateRequest: Codable {
    let title: String
    let content: String
}

struct ApplicationResponse: Codable {
    let id: Int
    let groupId: Int
    let groupTitle: String
    let status: String
    let appliedAt: String
}

struct GroupCreateRequest: Codable {
    let title: String
    let content: String
    let category: String
    let maxMembers: Int
}

/// Study/Team 생성용 요청 DTO (신규 API)
struct StudyTeamCreateRequest: Codable {
    let title: String
    let type: String
    let capacity: Int
    let startDate: String
    let endDate: String
    let campusId: Int
    let description: String
}

struct GroupApplyRequest: Codable {
    let groupId: Int
    let introduction: String
}

struct MemberResponse: Codable {
    let id: Int
    let nickname: String
    let role: String
}

struct ProgressResponse: Codable {
    let id: Int
    let title: String
    let date: String
    let status: String
}
