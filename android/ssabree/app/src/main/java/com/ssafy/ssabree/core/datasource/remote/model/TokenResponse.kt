package com.ssafy.ssabree.core.datasource.remote.model

data class TokenResponse(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSec: Long? = null,

    // optional: 서버가 같이 주면 저장해도 됨
    val uid: Int? = null,
    val userId: String? = null
)
