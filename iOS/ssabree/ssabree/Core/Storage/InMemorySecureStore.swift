import Foundation

final class InMemorySecureStore: SecureStore {
    private var tokens: AuthTokens?

    func saveTokens(_ tokens: AuthTokens) {
        self.tokens = tokens
    }

    func getTokens() -> AuthTokens? {
        tokens
    }

    func clearTokens() {
        tokens = nil
    }
}
