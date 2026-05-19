import Foundation

/// Response for GET /api/members/me endpoint
struct MeResponse: Codable {
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
