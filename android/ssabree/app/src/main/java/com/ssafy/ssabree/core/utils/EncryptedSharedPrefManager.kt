package com.ssafy.ssabree.core.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.ssafy.ssabree.core.utils.model.AuthTokens
import com.ssafy.ssabree.core.utils.model.UserInfo

class EncryptedSharedPrefManager(
    context: Context
) : SecureStorage {

    private companion object {
        const val ENCRYPTED_PREFERENCES_NAME = "ssabree_preference"

        const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"
        const val KEY_REFRESH_TOKEN = "REFRESH_TOKEN"
        const val KEY_TOKEN_TYPE = "TOKEN_TYPE"
        const val KEY_ACCESS_EXPIRES_AT = "ACCESS_EXPIRES_AT"

        // optional
        const val KEY_UID = "UID"
        const val KEY_USER_ID = "USER_ID"
    }

    private val prefs = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFERENCES_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveTokens(tokens: AuthTokens) {
        prefs.edit(commit = true) {
            putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            putString(KEY_TOKEN_TYPE, tokens.tokenType)

            // nullable long은 없으니 -1로 센티넬 처리
            val expiresAt = tokens.accessTokenExpiresAt ?: -1L
            putLong(KEY_ACCESS_EXPIRES_AT, expiresAt)

            // optional
            if (tokens.uid != null) putInt(KEY_UID, tokens.uid) else remove(KEY_UID)
            if (tokens.userId != null) putString(KEY_USER_ID, tokens.userId) else remove(KEY_USER_ID)
        }
    }

    override fun getTokens(): AuthTokens? {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null

        val tokenType = prefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"

        val expiresAtRaw = prefs.getLong(KEY_ACCESS_EXPIRES_AT, -1L)
        val expiresAt = expiresAtRaw.takeIf { it > 0L }

        val uid = if (prefs.contains(KEY_UID)) prefs.getInt(KEY_UID, -1).takeIf { it >= 0 } else null
        val userId = prefs.getString(KEY_USER_ID, null)

        return AuthTokens(
            accessToken = access,
            refreshToken = refresh,
            tokenType = tokenType,
            accessTokenExpiresAt = expiresAt,
            uid = uid,
            userId = userId
        )
    }

    override fun clearTokens() {
        prefs.edit(commit = true) { clear() }
    }

    // 편의 함수(선택): access 만료 판단이 필요하면 사용
    fun isAccessTokenExpired(nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiresAtRaw = prefs.getLong(KEY_ACCESS_EXPIRES_AT, -1L)
        if (expiresAtRaw <= 0L) return false // 만료 시각을 모르면 "만료 아님"으로 두고 401로 갱신 처리
        return nowMs >= expiresAtRaw
    }
}


//    fun addUserCookie(cookies: HashSet<String>) {
//        val editor = prefs.edit()
//        editor.putStringSet(COOKIES_KEY_NAME, cookies)
//        editor.apply()
//    }
//
//    fun getUserCookie(): MutableSet<String>? {
//        return prefs.getStringSet(COOKIES_KEY_NAME, HashSet())
//    }
//
//    fun deleteUserCookie() {
//        prefs.edit().remove(COOKIES_KEY_NAME).apply()
//    }
//
//    fun addNotice(info: String) {
//        val list = getNotice()
//
//        list.add(info)
//        val json = Gson().toJson(list)
//
//        prefs.edit().let {
//            it.putString("notice", json)
//            it.apply()
//        }
//    }
//
//    fun setNotice(list: MutableList<String>) {
//        prefs.edit().let {
//            it.putString("notice", Gson().toJson(list))
//            it.apply()
//        }
//    }

//    fun getNotice() : MutableList<String> {
//        val str = prefs.getString("notice", "")!!
//        val list = if(str.isEmpty()) mutableListOf<String>() else Gson().fromJson(str, MutableList::class.java) as MutableList<String>
//
//        return list
//    }

//    // Sharedprefs 관련 작업 처리
//    fun saveData(key: String, value: String) {
//        prefs.edit().putString(key, value).apply()
//    }
//
//    fun getData(key: String): String? {
//        return prefs.getString(key, null)
//    }
//


