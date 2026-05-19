package com.ssafy.ssabree.core.datasource.remote.model

data class SsafyConfirmRequest(
    val targetUserId: String,
    val authCode: String
)
