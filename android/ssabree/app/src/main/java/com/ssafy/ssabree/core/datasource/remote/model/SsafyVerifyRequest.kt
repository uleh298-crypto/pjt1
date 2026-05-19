package com.ssafy.ssabree.core.datasource.remote.model

data class SsafyVerifyRequest(
    val targetUserId: String,
    val generation: Int,
    val name: String
)
