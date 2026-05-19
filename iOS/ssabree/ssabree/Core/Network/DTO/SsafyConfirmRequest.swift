import Foundation

struct SsafyConfirmRequest: Encodable {
    let targetUserId: String
    let authCode: String
}
