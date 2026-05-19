package com.ssafy.ssabree.core.utils.model

// JWT 인증에 맞춘 저장 모델
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    /**
     * access token 만료 시각(에포크 ms). 서버가 expiresIn을 주면 계산해서 넣으면 됨.
     * 모르면 null로 두고, 401 나오면 refresh 하는 방식으로도 운영 가능.
     */
    val accessTokenExpiresAt: Long? = null,
    /**
     * 선택: 서버/클라에서 자주 쓰는 식별자. 굳이 저장 안 해도 됨.
     */
    val uid: Int? = null,
    val userId: String? = null
)