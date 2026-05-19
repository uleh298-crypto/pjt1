package com.ssafy.ssabree.core.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val DEFAULT_ZONE = ZoneId.of("Asia/Seoul")
private val DATE_OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val DATE_TIME_OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val DATE_TIME_NO_YEAR_FORMAT = DateTimeFormatter.ofPattern("MM.dd HH:mm")
private val TIME_ONLY_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

/**
 * 주어진 날짜 문자열을 KST 기준 상대시간 문자열로 변환한다.
 *
 * 규칙:
 * - 1분 미만: "지금"
 * - 1시간 미만: "N분 전"
 * - 1일 미만: "N시간 전"
 * - 7일 미만: "N일 전"
 * - 7일 이상: "yyyy.MM.dd"
 *
 * 입력 문자열이 Offset/Timezone 정보가 없으면 KST로 간주한다.
 */
fun formatRelativeTime(dateTimeString: String?, zone: ZoneId = DEFAULT_ZONE, now: ZonedDateTime = ZonedDateTime.now(zone)): String {
    if (dateTimeString.isNullOrBlank()) return ""

    val target = parseToZone(dateTimeString, zone)
    val diff = Duration.between(target, now)

    return when {
        diff.isNegative || diff.toMinutes() < 1 -> "지금"
        diff.toHours() < 1 -> "${diff.toMinutes()}분 전"
        diff.toDays() < 1 -> "${diff.toHours()}시간 전"
        diff.toDays() < 7 -> "${diff.toDays()}일 전"
        else -> target.format(DATE_OUTPUT_FORMAT)
    }
}

private fun parseToZone(dateTimeString: String, zone: ZoneId): ZonedDateTime {
    return try {
        ZonedDateTime.parse(dateTimeString).withZoneSameInstant(zone)
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(dateTimeString).toZonedDateTime().withZoneSameInstant(zone)
        } catch (_: Exception) {
            // Offset/zone 없는 경우 -> KST로 간주 (서버가 KST 기준)
            LocalDateTime.parse(dateTimeString).atZone(zone)
        }
    }
}

/**
 * Post/Comment 등에서 바로 쓸 수 있는 확장 함수.
 */
fun String?.toRelativeTimeText(): String = formatRelativeTime(this)

/**
 * 절대 시각 표기 (기본: yyyy.MM.dd HH:mm, KST 기준).
 * 입력에 Offset/Timezone 없으면 KST로 간주한다.
 */
fun formatAbsoluteKst(dateTimeString: String?, pattern: String = "yyyy.MM.dd HH:mm", zone: ZoneId = DEFAULT_ZONE): String {
    if (dateTimeString.isNullOrBlank()) return ""
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return try {
        parseToZone(dateTimeString, zone).format(formatter)
    } catch (_: Exception) {
        dateTimeString
    }
}

fun String?.toAbsoluteKstText(pattern: String = "yyyy.MM.dd HH:mm"): String =
    formatAbsoluteKst(this, pattern)

/**
 * 올해이면 연도 생략(MM.dd HH:mm), 그 외에는 yyyy.MM.dd HH:mm으로 포맷.
 */
fun formatAdaptiveKst(dateTimeString: String?, zone: ZoneId = DEFAULT_ZONE): String {
    if (dateTimeString.isNullOrBlank()) return ""
    return try {
        val zoned = parseToZone(dateTimeString, zone)
        val formatter = if (zoned.year == ZonedDateTime.now(zone).year) {
            DATE_TIME_NO_YEAR_FORMAT
        } else {
            DATE_TIME_OUTPUT_FORMAT
        }
        zoned.format(formatter)
    } catch (_: Exception) {
        dateTimeString
    }
}

fun String?.toAdaptiveKstText(): String = formatAdaptiveKst(this)

/**
 * 채팅 전용 시각 포맷:
 * - 오늘: HH:mm
 * - 올해: MM.dd HH:mm
 * - 그 외: yyyy.MM.dd HH:mm
 */
fun formatChatTime(dateTimeString: String?, zone: ZoneId = DEFAULT_ZONE, now: ZonedDateTime = ZonedDateTime.now(zone)): String {
    if (dateTimeString.isNullOrBlank()) return ""
    return try {
        val zoned = parseToZone(dateTimeString, zone)
        val formatter = when {
            zoned.toLocalDate() == now.toLocalDate() -> TIME_ONLY_FORMAT
            zoned.year == now.year -> DATE_TIME_NO_YEAR_FORMAT
            else -> DATE_TIME_OUTPUT_FORMAT
        }
        zoned.format(formatter)
    } catch (_: Exception) {
        dateTimeString
    }
}

fun String?.toChatTimeText(): String = formatChatTime(this)
