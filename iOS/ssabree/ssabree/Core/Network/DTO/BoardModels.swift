import Foundation

struct BoardResponse: Codable {
    let id: Int
    let name: String
    let category: String?
    let description: String?
}

struct BoardNoticeResponse: Codable {
    let id: Int
    let content: String?
}

struct BoardThumbResponse: Codable {
    let id: Int
    let title: String
    let content: String
    let userNickname: String
    let date: String
    let likeCount: Int
    let commentCount: Int
    // 백엔드가 camelCase로 반환하므로 CodingKeys 불필요
}
