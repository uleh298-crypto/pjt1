package com.ssafy.ssabree.core.utils

import com.ssafy.ssabree.core.utils.model.AuthTokens
import com.ssafy.ssabree.core.utils.model.UserInfo

interface SecureStorage {
    fun saveTokens(tokens: AuthTokens)
    fun getTokens(): AuthTokens?
    fun clearTokens()
}