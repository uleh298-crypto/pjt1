package com.ssafy.ssabree.features.mygroup.model

import com.ssafy.ssabree.core.repository.model.GroupSummaryModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupTypeMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MyGroupItemUiModel(
    val id: Long,
    val title: String,
    val role: String,
    val category: String,
    val currentMembers: Int,
    val maxMembers: Int,
    val status: String,
    val isLeader: Boolean,
    val isStudy: Boolean,
    val memberProfileImageUrls: List<String> = emptyList()
)

fun GroupSummaryModel.toMyGroupUiModel(kind: GroupKind, currentUserId: Long? = null): MyGroupItemUiModel {
    val categoryLabel = if (kind == GroupKind.STUDY) {
        GroupTypeMapper.studyTypeToLabel(type)
    } else {
        GroupTypeMapper.teamTypeToLabel(type)
    }
    // Determine if user is the leader
    val isUserLeader = currentUserId != null && leader?.id == currentUserId

    // Use API status if available, otherwise calculate from dates
    val displayStatus = when (status?.uppercase()) {
        "OPEN" -> "모집중"
        "ONGOING" -> "진행중"
        "CLOSED" -> "완료"
        else -> calculateStatus(startDate, endDate)
    }

    return MyGroupItemUiModel(
        id = id,
        title = title,
        role = if (isUserLeader) "팀장" else "팀원",
        category = categoryLabel,
        currentMembers = currentMembers ?: 0,
        maxMembers = capacity,
        status = displayStatus,
        isLeader = isUserLeader,
        isStudy = kind == GroupKind.STUDY
    )
}

private fun calculateStatus(startDate: String, endDate: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val now = Date()
    return runCatching {
        val start = format.parse(startDate)
        val end = format.parse(endDate)
        if (start != null && end != null) {
            if (now.before(start)) "모집중"
            else if (!now.after(end)) "진행중"
            else "완료"
        } else {
            "진행중"
        }
    }.getOrDefault("진행중")
}
