package com.ssafy.ssabree.core.utils

import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.datasource.remote.AuthService
import com.ssafy.ssabree.core.datasource.remote.model.RefreshRequest
import com.ssafy.ssabree.core.utils.model.AuthTokens
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

class AuthAuthenticator(
    private val authDataStore: AuthDataStore,
    private val authService: AuthService
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath

        // 로그인 엔드포인트는 401이 정상적인 응답이므로 제외
        if (path.endsWith("/api/auth/login")) {
            return null
        }

        // refresh 엔드포인트 401은 아래 catch에서 처리
        if (path.endsWith("/api/auth/refresh")) {
            return null
        }

        // 재시도 횟수 초과 시 강제 로그아웃
        if (responseCount(response) >= 2) {
            forceLogout(AuthLogoutReason.RETRY_EXCEEDED)
            return null
        }

        // refreshToken이 없으면 강제 로그아웃
        val refreshToken = authDataStore.getRefreshToken()
        if (refreshToken == null) {
            forceLogout(AuthLogoutReason.REFRESH_TOKEN_MISSING)
            return null
        }

        val newTokens = try {
            runBlocking {
                authService.refresh(RefreshRequest(refreshToken = refreshToken))
            }
        } catch (e: Exception) {
            // 네트워크 오류가 아닌 경우 강제 로그아웃
            val isNetworkError = e is IOException
            if (!isNetworkError) {
                forceLogout(AuthLogoutReason.REFRESH_FAILED)
            }
            return null
        }

        val expiresAt = newTokens.expiresInSec?.let { sec ->
            System.currentTimeMillis() + sec * 1000L
        }

        runBlocking {
            authDataStore.saveTokens(
                AuthTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    tokenType = newTokens.grantType,
                    accessTokenExpiresAt = expiresAt,
                    uid = newTokens.uid,
                    userId = newTokens.userId
                )
            )
        }

        return response.request.newBuilder()
            .header("Authorization", "${newTokens.grantType} ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private fun forceLogout(reason: AuthLogoutReason) {
        runBlocking { authDataStore.clear() }
        AuthEventBus.send(AuthEvent.Logout(reason))
    }
}
