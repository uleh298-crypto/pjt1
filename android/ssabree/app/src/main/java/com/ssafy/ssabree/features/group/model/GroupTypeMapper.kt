package com.ssafy.ssabree.features.group.model

object GroupTypeMapper {
    fun studyFilterLabels(): List<String> =
        listOf("전체", "알고리즘", "CS", "자격증", "기타")

    fun teamFilterLabels(): List<String> =
        listOf("전체", "싸피", "공모전", "자유")

    fun studyLabelToApi(label: String): String = when (label) {
        "알고리즘" -> "ALGORITHM"
        "CS" -> "CS"
        "A형" -> "SW_TEST_A"
        "B형" -> "SW_TEST_B"
        "자격증" -> "CERTIFICATION"
        "기타" -> "ETC"
        else -> "ETC"
    }

    fun teamLabelToApi(label: String): String = when (label) {
        "싸피" -> "SSAFY"
        "공모전" -> "CONTEST"
        "자유" -> "FREE"
        else -> "FREE"
    }

    fun studyTypeToLabel(type: String): String = when (type) {
        "ALGORITHM" -> "알고리즘"
        "CS" -> "CS"
        "SW_TEST_A" -> "A형"
        "SW_TEST_B" -> "B형"
        "CERTIFICATION" -> "자격증"
        "ETC" -> "기타"
        else -> type
    }

    fun teamTypeToLabel(type: String): String = when (type) {
        "SSAFY" -> "싸피"
        "CONTEST" -> "공모전"
        "FREE" -> "자유"
        else -> type
    }
}
