package com.ssafy.ssabree.core.datasource.remote.model

data class InquiryResponse(
    val inquiryId: Long,
    val content: String,
    val answer: String?,
    val createdAt: String?
)

data class InquiryListResponse(
    val items: List<InquiryResponse>
)

data class SuccessResponse(
    val success: Boolean
)
