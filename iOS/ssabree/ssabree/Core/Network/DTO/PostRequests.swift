import Foundation

struct PostCreateRequest: Codable {
    let title: String
    let content: String
    let boardId: Int
    let imageUrls: [String]
    let poll: PollCreateRequest?
}

struct PollCreateRequest: Codable {
    let title: String
    let options: [String]
}

struct PostUpdateRequest: Codable {
    let title: String
    let content: String
}

struct CommentCreateRequest: Codable {
    let content: String
}

struct ReplyCreateRequest: Codable {
    let content: String
}

struct VoteRequest: Codable {
    let optionId: Int
}
