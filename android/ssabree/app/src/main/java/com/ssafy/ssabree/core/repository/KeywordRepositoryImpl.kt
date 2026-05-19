package com.ssafy.ssabree.core.repository

import android.content.Context
import com.ssafy.ssabree.app.ApplicationClass

class KeywordRepositoryImpl : KeywordRepository {

    private val prefs = ApplicationClass.appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "recent_keywords_prefs"
        private const val KEY_KEYWORDS = "recent_keywords"
        private const val MAX_KEYWORDS = 10
        private const val DELIMITER = "|||"
    }

    override fun getRecentKeywords(): List<String> {
        val stored = prefs.getString(KEY_KEYWORDS, null) ?: return emptyList()
        return stored.split(DELIMITER).filter { it.isNotEmpty() }
    }

    override fun saveKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return

        val current = getRecentKeywords().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)

        val updated = current.take(MAX_KEYWORDS)
        prefs.edit().putString(KEY_KEYWORDS, updated.joinToString(DELIMITER)).apply()
    }

    override fun deleteKeyword(keyword: String) {
        val current = getRecentKeywords().toMutableList()
        current.remove(keyword)
        prefs.edit().putString(KEY_KEYWORDS, current.joinToString(DELIMITER)).apply()
    }

    override fun clearAll() {
        prefs.edit().remove(KEY_KEYWORDS).apply()
    }
}
