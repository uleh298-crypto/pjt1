import Foundation

final class FakeAuthRepository: AuthRepository {
    func login(email: String, password: String) async -> Result<Void, Error> { .success(()) }
    func refreshTokens() async -> Result<Void, Error> { .success(()) }
    func logout() async -> Result<Void, Error> { .success(()) }
    func isLoggedIn() -> Bool { true }
    func isEmailUsed(email: String) async -> Result<Bool, Error> { .success(false) }
    func signUp(info: SignUpInfo) async -> Result<Void, Error> { .success(()) }
    func requestSsafyVerification(mattermostId: String, generation: Int, name: String) async -> Result<Void, Error> { .success(()) }
    func confirmSsafyVerification(mattermostId: String, code: String) async -> Result<Void, Error> { .success(()) }
    func findId(mattermostId: String) async -> Result<String, Error> { .success("fake@example.com") }
    func resetPassword(mattermostId: String, newPassword: String) async -> Result<Void, Error> { .success(()) }
    func withdraw() async -> Result<Void, Error> { .success(()) }
    func getMyMemberId() async -> Result<Int, Error> { .success(1) }
}
