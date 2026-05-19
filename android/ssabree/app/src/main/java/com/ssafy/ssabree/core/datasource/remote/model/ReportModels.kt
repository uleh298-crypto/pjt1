package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class ReportCreateRequest(
    @SerializedName("targetType")
    val targetType: String,  // "POST" or "COMMENT"
    @SerializedName("targetId")
    val targetId: Long,
    @SerializedName("reason")
    val reason: String,  // "ABUSE", "SPAM", "INAPPROPRIATE", "OTHER"
    @SerializedName("detail")
    val detail: String? = null
)

data class ReportCreateResponse(
    @SerializedName("reportId")
    val reportId: Long,
    @SerializedName("createdAt")
    val createdAt: String
)
