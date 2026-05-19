package com.ssafy.ssabree.core.repository.model

enum class NotificationType(val value: String) {
    COMMENT("COMMENT"),
    REPLY("REPLY"),
    MESSAGE("MESSAGE"),
    HOT_POST("HOT_POST"),
    NOTICE("NOTICE"),
    ETC("ETC");

    companion object {
        fun fromString(value: String): NotificationType {
            return entries.find { it.value == value } ?: ETC
        }
    }
}

data class NotificationModel(
    val id: Long,
    val content: String,
    val isRead: Boolean,
    val relatedUrl: String?,
    val type: NotificationType,
    val createdAt: String
)
