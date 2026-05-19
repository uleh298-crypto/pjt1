package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.EmailCheckResponse
import com.ssafy.ssabree.core.datasource.remote.model.FindIdResponse
import com.ssafy.ssabree.core.datasource.remote.model.LoginRequest
import com.ssafy.ssabree.core.datasource.remote.model.RefreshRequest
import com.ssafy.ssabree.core.datasource.remote.model.ResetPasswordRequest
import com.ssafy.ssabree.core.datasource.remote.model.ResetPasswordResponse
import com.ssafy.ssabree.core.datasource.remote.model.SignUpRequest
import com.ssafy.ssabree.core.datasource.remote.model.SsafyConfirmRequest
import com.ssafy.ssabree.core.datasource.remote.model.SsafyVerifyRequest
import com.ssafy.ssabree.core.datasource.remote.model.TokenResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse

    @POST("/api/auth/send")
    suspend fun requestSsafyVerification(
        @Body request: SsafyVerifyRequest
    ): Response<ResponseBody>

    @POST("/api/auth/verify")
    suspend fun confirmSsafyVerification(
        @Body request: SsafyConfirmRequest
    ): Response<ResponseBody>

    @POST("/api/members/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<ResponseBody>

    @GET("/api/members/check-email")
    suspend fun checkEmailAvailable(@Query("email") email: String): EmailCheckResponse

    @GET("/api/auth/findId")
    suspend fun findId(@Query("mattermostId") mattermostId: String): FindIdResponse

    @POST("/api/auth/resetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    @DELETE("/api/members/me")
    suspend fun deleteMe(): Response<ResponseBody>
}
