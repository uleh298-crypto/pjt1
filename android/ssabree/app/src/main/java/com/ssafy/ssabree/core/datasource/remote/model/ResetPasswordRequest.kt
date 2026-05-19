package com.ssafy.ssabree.core.datasource.remote.model

data class ResetPasswordRequest(
    val mattermostId: String,
    val newPassword: String
)
