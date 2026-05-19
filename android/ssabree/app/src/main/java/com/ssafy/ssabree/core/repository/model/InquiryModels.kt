package com.ssafy.ssabree.core.repository.model

data class InquiryModel(
    val id: Long,
    val content: String,
    val answer: String?,
    val createdAt: String?
)
