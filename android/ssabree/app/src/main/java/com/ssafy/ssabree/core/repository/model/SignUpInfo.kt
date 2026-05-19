package com.ssafy.ssabree.core.repository.model

data class SignUpInfo(
    val email: String,
    val password: String,
    val name: String,
    val studentNo: Int?,
    val campus: Int?,
    val generation: Int?,
    val classNo: Int?,
    val mattermostId: String
)
