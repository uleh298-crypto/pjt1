package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.NotificationRepository
import com.ssafy.ssabree.core.repository.model.NotificationModel
import com.ssafy.ssabree.core.repository.model.NotificationType

class FakeNotificationRepository : NotificationRepository {

    private val fakeNotifications = mutableListOf(
        NotificationModel(
            id = 1L,
            content = "내 글에 댓글이 달렸습니다.",
            isRead = false,
            relatedUrl = "/posts/1",
            type = NotificationType.COMMENT,
            createdAt = "2025-01-21T12:00:00"
        ),
        NotificationModel(
            id = 2L,
            content = "내 댓글에 답글이 달렸습니다.",
            isRead = false,
            relatedUrl = "/posts/2",
            type = NotificationType.REPLY,
            createdAt = "2025-01-21T11:30:00"
        ),
        NotificationModel(
            id = 3L,
            content = "새로운 메시지가 도착했습니다.",
            isRead = true,
            relatedUrl = "/messages/1",
            type = NotificationType.MESSAGE,
            createdAt = "2025-01-21T10:00:00"
        ),
        NotificationModel(
            id = 4L,
            content = "새로운 공지사항이 등록되었습니다.",
            isRead = true,
            relatedUrl = "/posts/3",
            type = NotificationType.NOTICE,
            createdAt = "2025-01-20T15:00:00"
        )
    )

    override suspend fun getNotifications(): Result<List<NotificationModel>> {
        return Result.success(fakeNotifications.toList())
    }

    override suspend fun markAsRead(id: Long): Result<Unit> {
        val index = fakeNotifications.indexOfFirst { it.id == id }
        if (index >= 0) {
            fakeNotifications[index] = fakeNotifications[index].copy(isRead = true)
        }
        return Result.success(Unit)
    }

    override suspend fun registerFcmToken(token: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateSetting(type: NotificationType, enabled: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun subscribeScheduledNotification(token: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun unsubscribeScheduledNotification(token: String): Result<Unit> {
        return Result.success(Unit)
    }
}
