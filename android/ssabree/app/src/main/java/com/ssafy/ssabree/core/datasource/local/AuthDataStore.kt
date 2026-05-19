package com.ssafy.ssabree.core.datasource.local

import com.ssafy.ssabree.core.utils.SecureStorage
import com.ssafy.ssabree.core.utils.model.AuthTokens

class AuthDataStore(
    private val secureStorage: SecureStorage
) {
    fun isLoggedIn(): Boolean {
        return secureStorage.getTokens() != null
    }

    fun getAccessToken(): String? {
        return secureStorage.getTokens()?.accessToken
    }

    fun getTokenType(): String {
        return secureStorage.getTokens()?.tokenType ?: "Bearer"
    }

    fun getUId(): Int? {
        return secureStorage.getTokens()?.uid
    }

    fun getRefreshToken(): String? {
        return secureStorage.getTokens()?.refreshToken
    }

    suspend fun saveTokens(tokens: AuthTokens) {
        secureStorage.saveTokens(tokens)
    }

    suspend fun clear() {
        secureStorage.clearTokens()
    }
}

