package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Long,
    val content: String,
    @SerializedName(value = "isRead", alternate = ["read"])
    val isRead: Boolean,
    val relatedUrl: String?,
    val type: String,
    val createdAt: String
)

data class FcmTokenRequest(
    val token: String
)

data class NotificationSettingRequest(
    val notificationType: String,
    val enabled: Boolean
)
