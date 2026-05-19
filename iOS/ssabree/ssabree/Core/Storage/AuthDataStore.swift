import Foundation

/// Auth data store that manages token storage and retrieval
/// Works with any SecureStore implementation (Keychain or InMemory)
final class AuthDataStore {
    private let secureStore: SecureStore

    init(secureStore: SecureStore) {
        self.secureStore = secureStore
    }

    // MARK: - Login State

    func isLoggedIn() -> Bool {
        getAccessToken() != nil
    }

    // MARK: - Token Access

    func getAccessToken() -> String? {
        secureStore.getTokens()?.accessToken
    }

    func getRefreshToken() -> String? {
        secureStore.getTokens()?.refreshToken
    }

    func getTokenType() -> String {
        secureStore.getTokens()?.tokenType ?? "Bearer"
    }

    func getTokens() -> AuthTokens? {
        secureStore.getTokens()
    }

    // MARK: - User Info

    func getUserId() -> String? {
        secureStore.getTokens()?.userId
    }

    func getUid() -> Int? {
        secureStore.getTokens()?.uid
    }

    // MARK: - Token Management

    func saveTokens(_ tokens: AuthTokens) {
        secureStore.saveTokens(tokens)
    }

    func clear() {
        secureStore.clearTokens()
    }
}
