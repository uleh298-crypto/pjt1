package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.SignUpInfo

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun refreshTokens(): Result<Unit>
    suspend fun logout(): Result<Unit>

    suspend fun isEmailUsed(email: String): Result<Boolean>
    suspend fun signUp(req: SignUpInfo): Result<Unit>

    suspend fun requestSsafyVerification(mattermostId: String, generation: Int, name: String): Result<Unit>
    suspend fun confirmSsafyVerification(mattermostId: String, code: String): Result<Unit>

    suspend fun requestPasswordResetCode(destination: String): Result<Unit>
    suspend fun confirmPasswordReset(code: String, newPassword: String): Result<Unit>

    suspend fun findId(mattermostId: String): Result<String>
    suspend fun resetPassword(mattermostId: String, newPassword: String): Result<Unit>

    suspend fun withdraw(): Result<Unit>
}
