package com.ssafy.ssabree.core.datasource.local

import android.content.Context

class NotificationSettingLocalStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isScheduledNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_SCHEDULED_ENABLED, true)
    }

    fun saveScheduledNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCHEDULED_ENABLED, enabled).apply()
    }

    private companion object {
        const val PREF_NAME = "notification_local_settings"
        const val KEY_SCHEDULED_ENABLED = "scheduled_notification_enabled"
    }
}
