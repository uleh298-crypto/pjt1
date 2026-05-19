import Foundation

protocol SecureStore {
    func saveTokens(_ tokens: AuthTokens)
    func getTokens() -> AuthTokens?
    func clearTokens()
}
