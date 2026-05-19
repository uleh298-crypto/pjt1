package com.ssafy.ssabree.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.MainActivity
import com.ssafy.ssabree.core.utils.AppForegroundTracker
import com.ssafy.ssabree.core.utils.ChatRoomPresence

class SsabreeFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SsabreeFCM"
        private const val CHANNEL_ID = "ssabree_notifications"
        private const val CHANNEL_NAME = "싸브리 알림"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        FcmTokenSyncer.syncIfAuthenticated()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "싸브리",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]?.uppercase() ?: return
        when (type) {
            "CHAT_NEW", "MESSAGE" -> handleChatNew(data)
            "POST_COMMENT", "COMMENT_REPLY", "COMMENT", "REPLY" -> handlePostNotification(data)
            "APPLICATION_NEW", "GROUP_APPLICATION", "NEW_APPLICANT", "APPLICANT" -> handleGroupApplication(data)
            "APPLICATION_ACCEPTED", "ACCEPTED", "JOIN_ACCEPTED" -> handleApplicationAccepted(data)
            "APPLICATION_REJECTED", "REJECTED" -> handleApplicationRejected(data)
            else -> {
                val title = data["title"] ?: "싸브리"
                val body = data["body"] ?: data["content"] ?: ""
                if (body.isNotEmpty()) showNotification(title, body, data)
            }
        }
    }

    private fun handleChatNew(data: Map<String, String>) {
        val roomId = data["roomId"]?.toLongOrNull() ?: return
        val title = data["title"] ?: "새 쪽지"
        val body = data["body"] ?: data["preview"] ?: "새 메시지가 도착했습니다."

        if (shouldSuppressChatNotification(roomId)) {
            Log.d(TAG, "handleChatNew: suppress notification (foreground & room $roomId active)")
            return
        }

        // 포그라운드 처리: 이벤트 전파 (수신자가 collect 중일 때만)
        PushEventBus.send(PushEvent.OpenChat(roomId))

        // 알림 표시
        val deepLink = Uri.parse("ssabree://chat/$roomId")
        showNotification(title, body, data, deepLink)
    }

    private fun handlePostNotification(data: Map<String, String>) {
        val postId = extractPostId(data) ?: return
        val isReply = data["type"]?.uppercase() in setOf("COMMENT_REPLY", "REPLY")
        val title = data["title"] ?: if (isReply) "새 답글" else "새 댓글"
        val body = data["body"] ?: data["content"] ?: "게시글에 새 댓글이 달렸습니다."

        PushEventBus.send(PushEvent.OpenPost(postId))

        val deepLink = Uri.parse("ssabree://post/$postId")
        showNotification(title, body, data, deepLink)
    }

    private fun extractPostId(data: Map<String, String>): Long? {
        val candidateKeys = listOf("postId", "postID", "boardId", "boardID")
        candidateKeys.forEach { key ->
            data[key]?.toLongOrNull()?.let { return it }
        }

        val relatedUrl = data["relatedUrl"] ?: data["url"] ?: data["link"]
        if (!relatedUrl.isNullOrBlank()) {
            Regex("/posts/(\\d+)").find(relatedUrl)?.groupValues?.getOrNull(1)?.toLongOrNull()
                ?.let { return it }
        }

        return null
    }

    private fun handleGroupApplication(data: Map<String, String>) {
        val groupId = extractGroupId(data) ?: return
        val groupType = extractGroupType(data)
        val title = data["title"] ?: "새 지원자"
        val body = data["body"] ?: data["content"] ?: "새로운 지원자가 있습니다."

        val deepLink = Uri.parse("ssabree://group-application/$groupType/$groupId")
        showNotification(title, body, data, deepLink)
    }

    private fun handleApplicationAccepted(data: Map<String, String>) {
        val groupId = extractGroupId(data) ?: return
        val groupType = extractGroupType(data)
        val title = data["title"] ?: "지원 수락"
        val body = data["body"] ?: data["content"] ?: "지원이 수락되었습니다."

        val deepLink = Uri.parse("ssabree://application-accepted/$groupType/$groupId")
        showNotification(title, body, data, deepLink)
    }

    private fun handleApplicationRejected(data: Map<String, String>) {
        val title = data["title"] ?: "지원 결과"
        val body = data["body"] ?: data["content"] ?: "지원이 반려되었습니다."

        // 거절 알림은 특별한 화면 이동 없이 알림만 표시
        showNotification(title, body, data)
    }

    private fun extractGroupId(data: Map<String, String>): Long? {
        val candidateKeys = listOf("groupId", "groupID", "studyId", "projectId")
        candidateKeys.forEach { key ->
            data[key]?.toLongOrNull()?.let { return it }
        }
        return null
    }

    private fun extractGroupType(data: Map<String, String>): String {
        val typeValue = data["groupType"] ?: data["category"] ?: ""
        return when (typeValue.uppercase()) {
            "TEAM", "PROJECT", "PROJECTS" -> "project"
            else -> "study"
        }
    }

    private fun shouldSuppressChatNotification(roomId: Long): Boolean {
        val isForeground = AppForegroundTracker.isForeground.value
        val activeRoom = ChatRoomPresence.activeRoomId.value
        return isForeground && activeRoom == roomId
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>, deepLink: Uri? = null) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "싸브리 앱 알림"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = if (deepLink != null) {
            Intent(Intent.ACTION_VIEW, deepLink).apply {
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data.forEach { (key, value) -> putExtra(key, value) }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
