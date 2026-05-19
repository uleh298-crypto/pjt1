import Foundation

struct ResetPasswordRequest: Encodable {
    let mattermostId: String
    let newPassword: String
}
