package com.ssafy.ssabree.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.ssafy.ssabree.core.designsystem.theme.ThemeMode

class ThemePreferenceManager(context: Context) {

    private companion object {
        const val PREF_NAME = "ssabree_theme_preference"
        const val KEY_THEME_MODE = "theme_mode"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun getThemeMode(): ThemeMode {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}
