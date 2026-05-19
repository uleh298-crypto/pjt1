import Foundation
import Security

/// Keychain based secure storage for auth tokens
/// Unlike InMemorySecureStore, this persists tokens across app restarts
final class KeychainSecureStore: SecureStore {
    private let service: String
    private let accessGroup: String?

    private let tokensKey = "auth_tokens"

    init(service: String = "com.ssafy.ssabree", accessGroup: String? = nil) {
        self.service = service
        self.accessGroup = accessGroup
    }

    // MARK: - SecureStore Protocol

    func saveTokens(_ tokens: AuthTokens) {
        do {
            let data = try JSONEncoder().encode(tokens)
            save(data: data, forKey: tokensKey)
        } catch {
            print("KeychainSecureStore: Failed to encode tokens - \(error)")
        }
    }

    func getTokens() -> AuthTokens? {
        guard let data = load(forKey: tokensKey) else {
            return nil
        }

        do {
            let tokens = try JSONDecoder().decode(AuthTokens.self, from: data)
            return tokens
        } catch {
            print("KeychainSecureStore: Failed to decode tokens - \(error)")
            return nil
        }
    }

    func clearTokens() {
        delete(forKey: tokensKey)
    }

    // MARK: - Private Keychain Operations

    private func save(data: Data, forKey key: String) {
        // First delete any existing item
        delete(forKey: key)

        var query = baseQuery(forKey: key)
        query[kSecValueData as String] = data
        query[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly

        let status = SecItemAdd(query as CFDictionary, nil)

        if status != errSecSuccess {
            print("KeychainSecureStore: Failed to save data - status: \(status)")
        }
    }

    private func load(forKey key: String) -> Data? {
        var query = baseQuery(forKey: key)
        query[kSecReturnData as String] = kCFBooleanTrue
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess else {
            if status != errSecItemNotFound {
                print("KeychainSecureStore: Failed to load data - status: \(status)")
            }
            return nil
        }

        return result as? Data
    }

    private func delete(forKey key: String) {
        let query = baseQuery(forKey: key)
        let status = SecItemDelete(query as CFDictionary)

        if status != errSecSuccess && status != errSecItemNotFound {
            print("KeychainSecureStore: Failed to delete data - status: \(status)")
        }
    }

    private func baseQuery(forKey key: String) -> [String: Any] {
        var query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]

        if let accessGroup = accessGroup {
            query[kSecAttrAccessGroup as String] = accessGroup
        }

        return query
    }
}
