import Foundation

struct SsafyVerifyRequest: Encodable {
    let targetUserId: String
    let generation: Int
    let name: String
}
