package com.ssafy.ssabree.core.datasource.remote.model

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
    val studentNo: Int?,
    val campus: Int?,
    val generation: Int?,
    val classNo: Int?,
    val mattermostId: String
)
