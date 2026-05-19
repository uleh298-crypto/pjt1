package com.ssafy.ssabree.core.repository

interface KeywordRepository {
    fun getRecentKeywords(): List<String>
    fun saveKeyword(keyword: String)
    fun deleteKeyword(keyword: String)
    fun clearAll()
}
