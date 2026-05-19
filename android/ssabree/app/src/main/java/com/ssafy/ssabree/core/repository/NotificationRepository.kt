package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.NotificationModel
import com.ssafy.ssabree.core.repository.model.NotificationType

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationModel>>
    suspend fun markAsRead(id: Long): Result<Unit>
    suspend fun registerFcmToken(token: String): Result<Unit>
    suspend fun updateSetting(type: NotificationType, enabled: Boolean): Result<Unit>
    suspend fun subscribeScheduledNotification(token: String): Result<Unit>
    suspend fun unsubscribeScheduledNotification(token: String): Result<Unit>
}
