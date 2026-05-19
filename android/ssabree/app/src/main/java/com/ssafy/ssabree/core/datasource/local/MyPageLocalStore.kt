package com.ssafy.ssabree.core.datasource.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.ssabree.core.repository.model.MyPageModel
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.PortfolioModel

data class CachedMyPage(
    val myPage: MyPageModel?
)

data class CachedPortfolioDetail(
    val portfolio: PortfolioModel?,
    val projects: List<ProjectModel>
)

class MyPageLocalStore(context: Context) {
    private companion object {
        const val PREF_NAME = "mypage_prefs"
        const val KEY_MYPAGE = "mypage_cache"
        const val KEY_PORTFOLIO_DETAIL = "portfolio_detail_cache"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadMyPage(): CachedMyPage? {
        val json = prefs.getString(KEY_MYPAGE, null) ?: return null
        return try {
            gson.fromJson(json, CachedMyPage::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveMyPage(cache: CachedMyPage) {
        val json = gson.toJson(cache)
        prefs.edit().putString(KEY_MYPAGE, json).apply()
    }

    fun loadPortfolioDetail(): CachedPortfolioDetail? {
        val json = prefs.getString(KEY_PORTFOLIO_DETAIL, null) ?: return null
        return try {
            val type = object : TypeToken<CachedPortfolioDetail>() {}.type
            gson.fromJson<CachedPortfolioDetail>(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun savePortfolioDetail(cache: CachedPortfolioDetail) {
        val json = gson.toJson(cache)
        prefs.edit().putString(KEY_PORTFOLIO_DETAIL, json).apply()
    }
}
