package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class DdayListResponse(
    val items: List<DdayResponse>
)

/**
 * 서버에서 내려주는 D-day 응답.
 * 예시:
 * {
 *   "items": [ { "ddayId": 301, "title": "중간고사 시작", "date": "2023-10-25", "icon": "CAP" } ]
 * }
 */
data class DdayResponse(
    @SerializedName("ddayId") val id: Int,
    val title: String,
    // 서버 필드는 "date"(yyyy-MM-dd). 기존 targetDate 명명을 유지하기 위해 SerializedName 사용
    @SerializedName("date") val targetDate: String,
    // 서버에는 남은 일수가 없으므로 아이콘 키만 추가로 내려온다.
    @SerializedName("icon") val iconKey: String?
)
