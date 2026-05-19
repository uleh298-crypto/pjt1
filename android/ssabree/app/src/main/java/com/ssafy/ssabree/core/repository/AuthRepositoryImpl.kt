package com.ssafy.ssabree.core.repository

import android.util.Log
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.datasource.remote.AuthService
import com.ssafy.ssabree.core.datasource.remote.MemberService
import com.ssafy.ssabree.core.datasource.remote.model.LoginRequest
import com.ssafy.ssabree.core.datasource.remote.model.RefreshRequest
import com.ssafy.ssabree.core.datasource.remote.model.ResetPasswordRequest
import com.ssafy.ssabree.core.datasource.remote.model.SsafyConfirmRequest
import com.ssafy.ssabree.core.datasource.remote.model.SsafyVerifyRequest
import com.ssafy.ssabree.core.datasource.remote.model.toSignUpRequest
import com.ssafy.ssabree.core.repository.model.SignUpInfo
import com.ssafy.ssabree.core.utils.FcmTokenSyncer
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.core.utils.model.AuthTokens
import org.json.JSONObject
import retrofit2.HttpException

class AuthRepositoryImpl : AuthRepository {
    private companion object {
        const val TAG = "AuthRepositoryImpl"
    }

    private val authDataStore = AuthDataStore(ApplicationClass.encryptedSharedPrefManager)
    private val authService = RetrofitClient.instance.create(AuthService::class.java)
    private val memberService = RetrofitClient.instance.create(MemberService::class.java)

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        val jsonMessage = runCatching {
            JSONObject(errorBody).optString("message")
        }.getOrNull()
        return jsonMessage?.takeIf { it.isNotBlank() } ?: errorBody
    }

    private fun parseErrorCode(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return runCatching {
            JSONObject(errorBody).optString("code")
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: HttpException) {
            val message = parseErrorMessage(e.response()?.errorBody()?.string())
            if (!message.isNullOrBlank()) {
                throw IllegalStateException(message)
            }
            throw e
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return runCatching {
            val res = safeCall {
                authService.login(LoginRequest(email = email, password = password))
            }

            val expiresAt = res.expiresInSec?.let { sec ->
                System.currentTimeMillis() + sec * 1000L
            }

            val accessSuffix = res.accessToken.takeLast(6)
            val refreshSuffix = res.refreshToken.takeLast(6)
            Log.d(
                "AuthRepository",
                "login token received (access suffix): $accessSuffix / refresh suffix: $refreshSuffix"
            )

            // 토큰 먼저 저장 (이후 API 호출에 필요)
            authDataStore.saveTokens(
                AuthTokens(
                    accessToken = res.accessToken,
                    refreshToken = res.refreshToken,
                    tokenType = res.grantType,
                    accessTokenExpiresAt = expiresAt,
                    uid = null, // 아래에서 /api/members/me 호출 후 업데이트
                    userId = res.userId
                )
            )

            // /api/members/me 호출해서 memberId 가져오기
            val memberId = runCatching {
                memberService.getMe().id.toInt()
            }.getOrNull()

            // memberId 포함해서 다시 저장
            authDataStore.saveTokens(
                AuthTokens(
                    accessToken = res.accessToken,
                    refreshToken = res.refreshToken,
                    tokenType = res.grantType,
                    accessTokenExpiresAt = expiresAt,
                    uid = memberId,
                    userId = res.userId
                )
            )

            Log.d(
                TAG,
                "login: saved tokens type=${res.grantType} " +
                    "access=${res.accessToken.take(10)}... " +
                    "refresh=${res.refreshToken.take(10)}... " +
                    "expiresAt=$expiresAt memberId=$memberId"
            )
            FcmTokenSyncer.syncIfAuthenticated()
        }
    }

    override suspend fun refreshTokens(): Result<Unit> {
        return runCatching {
            val refresh = authDataStore.getRefreshToken()
                ?: error("No refresh token")

            val res = safeCall {
                authService.refresh(
                    RefreshRequest(refreshToken = refresh)
                )
            }

            val expiresAt = res.expiresInSec?.let { sec ->
                System.currentTimeMillis() + sec * 1000L
            }

            authDataStore.saveTokens(
                AuthTokens(
                    accessToken = res.accessToken,
                    refreshToken = res.refreshToken,
                    tokenType = res.grantType,
                    accessTokenExpiresAt = expiresAt,
                    uid = res.uid,
                    userId = res.userId
                )
            )
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            authDataStore.clear()
        }
    }

    /**
     * 이메일 중복 확인
     * - true: 이메일 사용 중
     * - false: 사용 가능
     */
    override suspend fun isEmailUsed(email: String): Result<Boolean> {
        return runCatching {
            !safeCall { authService.checkEmailAvailable(email = email) }.unique
        }
    }

    /**
     * 회원가입
     * - Mattermost 인증 완료 후 요청
     */
    override suspend fun signUp(info: SignUpInfo): Result<Unit> {
        return runCatching {
            val body = info.toSignUpRequest()
            val response = safeCall { authService.signUp(request = body) }
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val message = parseErrorMessage(errorBody) ?: "회원가입에 실패했습니다."
                error(message)
            }
        }
    }

    /**
     * 1) Mattermost 인증번호 발송 요청
     */
    override suspend fun requestSsafyVerification(mattermostId: String, generation: Int, name: String): Result<Unit> {
        return runCatching {
            val response = safeCall {
                authService.requestSsafyVerification(
                    SsafyVerifyRequest(targetUserId = mattermostId, generation = generation, name = name)
                )
            }
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val message = parseErrorMessage(errorBody) ?: "인증번호 발송에 실패했습니다."
                error(message)
            }
        }
    }

    /**
     * 2) 인증번호 확인
     */
    override suspend fun confirmSsafyVerification(
        mattermostId: String,
        code: String
    ): Result<Unit> {
        return runCatching {
            val response = safeCall {
                authService.confirmSsafyVerification(
                    SsafyConfirmRequest(
                        targetUserId = mattermostId,
                        authCode = code
                    )
                )
            }
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val message = parseErrorMessage(errorBody) ?: "인증번호가 일치하지 않거나 만료되었습니다."
                error(message)
            }
        }
    }

    override suspend fun requestPasswordResetCode(destination: String): Result<Unit> {
        return Result.failure(IllegalStateException("Password reset API not implemented in AuthService"))
    }

    override suspend fun confirmPasswordReset(
        code: String,
        newPassword: String
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Password reset API not implemented in AuthService"))
    }

    override suspend fun findId(mattermostId: String): Result<String> {
        return runCatching {
            safeCall { authService.findId(mattermostId) }.userEmail
        }
    }

    override suspend fun resetPassword(mattermostId: String, newPassword: String): Result<Unit> {
        return runCatching {
            val response = safeCall {
                authService.resetPassword(
                    ResetPasswordRequest(
                        mattermostId = mattermostId,
                        newPassword = newPassword
                    )
                )
            }
            if (!response.success) {
                error("비밀번호 재설정에 실패했습니다.")
            }
        }
    }

    override suspend fun withdraw(): Result<Unit> {
        return runCatching {
            val response = safeCall { authService.deleteMe() }
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val code = parseErrorCode(errorBody)
                val message = if (code == "B012") {
                    "참여중인 팀 또는 스터디가 존재합니다. 탈퇴 이후 다시 진행해주세요."
                } else {
                    parseErrorMessage(errorBody) ?: "회원 탈퇴에 실패했습니다."
                }
                error(message)
            }
            authDataStore.clear()
        }
    }
}
