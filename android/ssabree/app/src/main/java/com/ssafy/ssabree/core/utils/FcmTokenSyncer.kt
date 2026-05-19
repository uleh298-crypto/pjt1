package com.ssafy.ssabree.core.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.datasource.remote.NotificationService
import com.ssafy.ssabree.core.datasource.remote.model.FcmTokenRequest
import com.ssafy.ssabree.core.datasource.local.NotificationSettingLocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "FcmTokenSyncer"

object FcmTokenSyncer {
    private val authDataStore =
        AuthDataStore(ApplicationClass.encryptedSharedPrefManager)
    private val notificationSettingStore =
        NotificationSettingLocalStore(ApplicationClass.appContext)

    /**
     * 로그인된 세션이 있을 때만 FCM 토큰을 서버와 동기화
     */
    fun syncIfAuthenticated() {
        val uid = authDataStore.getUId() ?: return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                syncToken(token)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "FCM token fetch failed", e)
            }
    }

    private fun syncToken(token: String) {
        if (token.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val service = RetrofitClient.instance.create(NotificationService::class.java)
                val request = FcmTokenRequest(token)
                service.registerToken(request)
                service.subscribe(request)
                notificationSettingStore.saveScheduledNotificationEnabled(true)
                Log.d(TAG, "FCM token synced successfully")
            }.onFailure { e ->
                Log.d(TAG, "FCM token sync failed", e)
            }
        }
    }
}
