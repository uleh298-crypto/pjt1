package com.ssafy.ssabree.core.utils

import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(
    private val authDataStore: AuthDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = authDataStore.getAccessToken()
        val tokenType = authDataStore.getTokenType()

        if (accessToken.isNullOrBlank()) {
            Log.d("AuthInterceptor", "No access token; proceed without Authorization")
            return chain.proceed(request)
        }

        Log.d("AuthInterceptor", "Attach Authorization: $tokenType ${accessToken.take(10)}...")
        val authedRequest = request.newBuilder()
            .header("Authorization", "$tokenType $accessToken")
            .build()
        return chain.proceed(authedRequest)
    }
}
