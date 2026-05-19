package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.KeywordRepository

class FakeKeywordRepository : KeywordRepository {

    private val keywords = mutableListOf("싸피", "싸피셜")

    override fun getRecentKeywords(): List<String> = keywords.toList()

    override fun saveKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        keywords.remove(trimmed)
        keywords.add(0, trimmed)
        if (keywords.size > 10) {
            keywords.removeAt(keywords.lastIndex)
        }
    }

    override fun deleteKeyword(keyword: String) {
        keywords.remove(keyword)
    }

    override fun clearAll() {
        keywords.clear()
    }
}
