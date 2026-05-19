package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.NotificationService
import com.ssafy.ssabree.core.datasource.remote.model.FcmTokenRequest
import com.ssafy.ssabree.core.datasource.remote.model.NotificationSettingRequest
import com.ssafy.ssabree.core.repository.model.NotificationModel
import com.ssafy.ssabree.core.repository.model.NotificationType
import com.ssafy.ssabree.core.utils.RetrofitClient

class NotificationRepositoryImpl : NotificationRepository {

    private val notificationService: NotificationService =
        RetrofitClient.instance.create(NotificationService::class.java)

    override suspend fun getNotifications(): Result<List<NotificationModel>> {
        return runCatching {
            notificationService.getNotifications().map { response ->
                NotificationModel(
                    id = response.id,
                    content = response.content,
                    isRead = response.isRead,
                    relatedUrl = response.relatedUrl,
                    type = NotificationType.fromString(response.type),
                    createdAt = response.createdAt
                )
            }
        }
    }

    override suspend fun markAsRead(id: Long): Result<Unit> {
        return runCatching {
            notificationService.markAsRead(id)
        }
    }

    override suspend fun registerFcmToken(token: String): Result<Unit> {
        return runCatching {
            notificationService.registerToken(FcmTokenRequest(token))
        }
    }

    override suspend fun updateSetting(type: NotificationType, enabled: Boolean): Result<Unit> {
        return runCatching {
            notificationService.updateSetting(
                NotificationSettingRequest(
                    notificationType = type.value,
                    enabled = enabled
                )
            )
        }
    }

    override suspend fun subscribeScheduledNotification(token: String): Result<Unit> {
        return runCatching {
            notificationService.subscribe(FcmTokenRequest(token))
        }
    }

    override suspend fun unsubscribeScheduledNotification(token: String): Result<Unit> {
        return runCatching {
            notificationService.unsubscribe(FcmTokenRequest(token))
        }
    }
}
