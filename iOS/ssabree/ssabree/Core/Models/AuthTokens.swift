import Foundation

struct AuthTokens: Codable {
    let accessToken: String
    let refreshToken: String
    let tokenType: String
    let userId: String?
    let uid: Int?

    init(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        userId: String? = nil,
        uid: Int? = nil
    ) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
        self.tokenType = tokenType
        self.userId = userId
        self.uid = uid
    }
}
