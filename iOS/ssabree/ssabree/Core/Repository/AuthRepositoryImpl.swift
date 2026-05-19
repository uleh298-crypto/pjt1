import Foundation

final class AuthRepositoryImpl: AuthRepository {
    private let authService: AuthService
    private let authDataStore: AuthDataStore

    init(authService: AuthService, authDataStore: AuthDataStore) {
        self.authService = authService
        self.authDataStore = authDataStore
    }

    func isLoggedIn() -> Bool {
        authDataStore.isLoggedIn()
    }

    func login(email: String, password: String) async -> Result<Void, Error> {
        do {
            let response = try await authService.login(
                LoginRequest(email: email, password: password)
            )

            // 먼저 토큰 저장 (getMe 호출에 필요)
            var tokens = AuthTokens(
                accessToken: response.accessToken,
                refreshToken: response.refreshToken,
                tokenType: response.grantType ?? "Bearer",
                userId: response.userId,
                uid: response.uid
            )

            authDataStore.saveTokens(tokens)

            // /api/members/me 호출해서 memberId 가져오기 (Android와 동일 패턴)
            if let memberId = try? await authService.getMe().id {
                tokens = AuthTokens(
                    accessToken: response.accessToken,
                    refreshToken: response.refreshToken,
                    tokenType: response.grantType ?? "Bearer",
                    userId: response.userId,
                    uid: memberId
                )
                authDataStore.saveTokens(tokens)
            }

            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func refreshTokens() async -> Result<Void, Error> {
        do {
            guard let refreshToken = authDataStore.getRefreshToken() else {
                return .failure(AuthError.missingRefreshToken)
            }

            // 기존 uid 보존
            let existingUid = authDataStore.getUid()

            let response = try await authService.refresh(
                RefreshRequest(refreshToken: refreshToken)
            )

            var tokens = AuthTokens(
                accessToken: response.accessToken,
                refreshToken: response.refreshToken,
                tokenType: response.grantType ?? "Bearer",
                userId: response.userId,
                uid: existingUid ?? response.uid  // 기존 uid 유지
            )

            authDataStore.saveTokens(tokens)

            // uid가 없으면 getMe 호출
            if tokens.uid == nil {
                if let memberId = try? await authService.getMe().id {
                    tokens = AuthTokens(
                        accessToken: response.accessToken,
                        refreshToken: response.refreshToken,
                        tokenType: response.grantType ?? "Bearer",
                        userId: response.userId,
                        uid: memberId
                    )
                    authDataStore.saveTokens(tokens)
                }
            }

            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func logout() async -> Result<Void, Error> {
        authDataStore.clear()
        return .success(())
    }

    func isEmailUsed(email: String) async -> Result<Bool, Error> {
        do {
            let response = try await authService.checkEmailAvailable(email: email)
            // unique == true means email is not used
            return .success(!response.unique)
        } catch {
            return .failure(error)
        }
    }

    func signUp(info: SignUpInfo) async -> Result<Void, Error> {
        do {
            let request = SignUpRequest(
                email: info.email,
                password: info.password,
                name: info.name,
                studentNo: Int(info.studentId),
                campus: Int(info.campus),
                generation: info.generation,
                classNo: info.clazz,
                mattermostId: info.mattermostId
            )
            try await authService.signUp(request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func requestSsafyVerification(mattermostId: String, generation: Int, name: String) async -> Result<Void, Error> {
        do {
            try await authService.requestSsafyVerification(
                SsafyVerifyRequest(targetUserId: mattermostId, generation: generation, name: name)
            )
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func confirmSsafyVerification(mattermostId: String, code: String) async -> Result<Void, Error> {
        do {
            try await authService.confirmSsafyVerification(
                SsafyConfirmRequest(targetUserId: mattermostId, authCode: code)
            )
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func findId(mattermostId: String) async -> Result<String, Error> {
        do {
            let response = try await authService.findId(mattermostId: mattermostId)
            return .success(response.userEmail)
        } catch {
            return .failure(error)
        }
    }

    func resetPassword(mattermostId: String, newPassword: String) async -> Result<Void, Error> {
        do {
            let response = try await authService.resetPassword(
                ResetPasswordRequest(mattermostId: mattermostId, newPassword: newPassword)
            )
            if response.success {
                return .success(())
            } else {
                return .failure(AuthError.passwordResetFailed)
            }
        } catch {
            return .failure(error)
        }
    }

    func withdraw() async -> Result<Void, Error> {
        do {
            try await authService.deleteMe()
            authDataStore.clear()
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func getMyMemberId() async -> Result<Int, Error> {
        // 로컬에 저장된 uid가 있으면 바로 반환
        if let uid = authDataStore.getUid() {
            return .success(uid)
        }

        // 없으면 API 호출 (Android의 memberRepository.getMyMemberId() 패턴)
        do {
            let response = try await authService.getMe()
            return .success(response.id)
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Auth Errors

enum AuthError: LocalizedError {
    case missingRefreshToken
    case passwordResetFailed
    case unknown(String)

    var errorDescription: String? {
        switch self {
        case .missingRefreshToken:
            return "로그인 정보가 없습니다"
        case .passwordResetFailed:
            return "비밀번호 재설정에 실패했습니다"
        case .unknown(let message):
            return message
        }
    }
}
