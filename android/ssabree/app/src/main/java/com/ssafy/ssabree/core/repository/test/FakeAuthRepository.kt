package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.AuthRepository
import com.ssafy.ssabree.core.repository.model.SignUpInfo

class FakeAuthRepository: AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return Result.success(Unit)
    }
    override suspend fun refreshTokens(): Result<Unit> {
        return Result.success(Unit)
    }
    override suspend fun logout(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun isEmailUsed(email: String): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun signUp(req: SignUpInfo): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun requestSsafyVerification(mattermostId: String, generation: Int, name: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun confirmSsafyVerification(
        mattermostId: String,
        code: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun requestPasswordResetCode(destination: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun confirmPasswordReset(
        code: String,
        newPassword: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun findId(mattermostId: String): Result<String> {
        return Result.success("test@example.com")
    }

    override suspend fun resetPassword(mattermostId: String, newPassword: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun withdraw(): Result<Unit> {
        return Result.success(Unit)
    }
}
