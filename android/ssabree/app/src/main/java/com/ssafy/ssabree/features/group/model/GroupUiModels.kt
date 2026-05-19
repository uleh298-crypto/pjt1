package com.ssafy.ssabree.features.group.model

import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupSummaryModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GroupListItemUiModel(
    val id: Long,
    val title: String,
    val type: String,
    val categoryLabel: String,
    val dDay: String,
    val currentMembers: Int,
    val maxMembers: Int,
    val endDate: String,
    val isExpired: Boolean,
    val isFull: Boolean,
    val isClosed: Boolean,
    val closedMessage: String?,
    val isStudy: Boolean,
    val dateRange: String
)

data class GroupDetailUiModel(
    val id: Long,
    val title: String,
    val typeLabel: String,
    val dDay: String,
    val dateRange: String,
    val endDateDisplay: String,
    val memberCountInfo: String,
    val capacity: Int,
    val description: String,
    val leaderName: String,
    val leaderMattermostId: String,
    val leaderProfileImageUrl: String?
)

fun GroupSummaryModel.toUiModel(kind: GroupKind, currentMembersOverride: Int? = null): GroupListItemUiModel {
    val categoryLabel = if (kind == GroupKind.STUDY) {
        GroupTypeMapper.studyTypeToLabel(type)
    } else {
        GroupTypeMapper.teamTypeToLabel(type)
    }
    val current = currentMembersOverride ?: currentMembers ?: 0
    val isExpired = isRecruitmentExpired(endDate)
    val isFull = current >= capacity
    val isClosed = isExpired || isFull
    val closedMessage = when {
        isExpired -> "모집 공고가 지났습니다."
        isFull -> "그룹 인원이 꽉 찼습니다."
        else -> null
    }
    return GroupListItemUiModel(
        id = id,
        title = title,
        type = type,
        categoryLabel = categoryLabel,
        dDay = calculateDDay(endDate),
        currentMembers = current,
        maxMembers = capacity,
        endDate = endDate,
        isExpired = isExpired,
        isFull = isFull,
        isClosed = isClosed,
        closedMessage = closedMessage,
        isStudy = kind == GroupKind.STUDY,
        dateRange = formatDateRange(startDate, endDate)
    )
}

fun GroupDetailModel.toUiModel(kind: GroupKind): GroupDetailUiModel {
    val typeLabel = if (kind == GroupKind.STUDY) {
        GroupTypeMapper.studyTypeToLabel(type)
    } else {
        GroupTypeMapper.teamTypeToLabel(type)
    }
    val current = currentMembers ?: 0
    return GroupDetailUiModel(
        id = id,
        title = title,
        typeLabel = typeLabel,
        dDay = calculateDDay(endDate),
        dateRange = formatDateRange(startDate, endDate),
        endDateDisplay = formatEndDate(endDate),
        memberCountInfo = "${current}/${capacity}명",
        capacity = capacity,
        description = description,
        leaderName = leaderName ?: "-",
        leaderMattermostId = leaderMattermostId ?: "-",
        leaderProfileImageUrl = leaderProfileImageUrl
    )
}

private fun formatEndDate(endDate: String): String {
    val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val output = SimpleDateFormat("yy/MM/dd", Locale.KOREA)
    return runCatching {
        val end = input.parse(endDate)
        if (end != null) {
            output.format(end)
        } else {
            "미정"
        }
    }.getOrDefault("미정")
}

private fun formatDateRange(startDate: String, endDate: String): String {
    val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val output = SimpleDateFormat("yy/MM/dd", Locale.KOREA)
    return runCatching {
        val start = input.parse(startDate)
        val end = input.parse(endDate)
        if (start != null && end != null) {
            "${output.format(start)} ~ ${output.format(end)}"
        } else {
            "기간 미정"
        }
    }.getOrDefault("기간 미정")
}

private fun calculateDDay(endDate: String): String {
    val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    return runCatching {
        val end = input.parse(endDate)
        if (end != null) {
            val diff = end.time - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            if (days >= 0) "D-$days" else "모집 종료"
        } else {
            "상시"
        }
    }.getOrDefault("상시")
}

private fun isRecruitmentExpired(endDate: String): Boolean {
    val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    return runCatching {
        val end = input.parse(endDate) ?: return@runCatching false
        val today = input.parse(input.format(Date()))
        if (today == null) false else end.before(today)
    }.getOrDefault(false)
}
