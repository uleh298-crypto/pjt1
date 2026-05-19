package com.ssafy.ssabree.core.datasource.remote.model

data class BoardResponse(
    val id: Long,
    val name: String,
    val category: String?,
    val description: String?
)

data class BoardNoticeResponse(
    val id: Long,
    val content: String?
)
