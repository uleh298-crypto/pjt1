import Foundation

struct TokenResponse: Decodable {
    let grantType: String?
    let accessToken: String
    let refreshToken: String
    let expiresInSec: Int64?
    let uid: Int?
    let userId: String?
}
