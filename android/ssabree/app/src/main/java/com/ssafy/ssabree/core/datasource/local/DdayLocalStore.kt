package com.ssafy.ssabree.core.datasource.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LocalDdayItem(
    val id: Int,
    val title: String,
    val date: String,
    val showOnHome: Boolean,
    val iconKey: String?
)

class DdayLocalStore(
    context: Context
) {
    private companion object {
        const val PREF_NAME = "dday_prefs"
        const val KEY_ITEMS = "dday_items"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): List<LocalDdayItem> {
        val json = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LocalDdayItem>>() {}.type
            gson.fromJson<List<LocalDdayItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(items: List<LocalDdayItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_ITEMS, json).apply()
    }
}
