import Foundation

protocol AuthRepository {
    func login(email: String, password: String) async -> Result<Void, Error>
    func refreshTokens() async -> Result<Void, Error>
    func logout() async -> Result<Void, Error>
    func isLoggedIn() -> Bool

    func isEmailUsed(email: String) async -> Result<Bool, Error>
    func signUp(info: SignUpInfo) async -> Result<Void, Error>

    func requestSsafyVerification(mattermostId: String, generation: Int, name: String) async -> Result<Void, Error>
    func confirmSsafyVerification(mattermostId: String, code: String) async -> Result<Void, Error>

    func findId(mattermostId: String) async -> Result<String, Error>
    func resetPassword(mattermostId: String, newPassword: String) async -> Result<Void, Error>

    func withdraw() async -> Result<Void, Error>

    func getMyMemberId() async -> Result<Int, Error>
}
