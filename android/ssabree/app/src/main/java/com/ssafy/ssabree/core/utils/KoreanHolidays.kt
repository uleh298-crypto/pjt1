package com.ssafy.ssabree.core.utils

import java.util.Calendar

/**
 * 대한민국 공휴일 판별 유틸리티
 * 2025~2030년 음력 공휴일(설날, 추석, 부처님오신날) 및 대체공휴일 포함
 */
object KoreanHolidays {

    /**
     * 연도별 변동 공휴일 (음력 기반 + 대체공휴일)
     * Key: 연도, Value: (월(0-indexed), 일) 리스트
     */
    private val variableHolidays: Map<Int, List<Pair<Int, Int>>> = mapOf(
        2025 to listOf(
            // 설날 연휴: 1/27(월)~1/30(목) - 1/27 대체공휴일 포함
            Pair(0, 27), Pair(0, 28), Pair(0, 29), Pair(0, 30),
            // 삼일절 대체공휴일: 3/3(월) - 3/1이 토요일
            Pair(2, 3),
            // 부처님오신날: 5/5(월) - 어린이날과 겹침
            Pair(4, 5),
            // 어린이날 대체공휴일: 5/6(화)
            Pair(4, 6),
            // 추석 연휴: 10/5(일)~10/7(화) + 대체공휴일 10/8(수)
            Pair(9, 5), Pair(9, 6), Pair(9, 7), Pair(9, 8),
        ),
        2026 to listOf(
            // 설날 연휴: 2/16(월)~2/18(수)
            Pair(1, 16), Pair(1, 17), Pair(1, 18),
            // 부처님오신날: 5/24(일) + 대체공휴일 5/25(월)
            Pair(4, 24), Pair(4, 25),
            // 추석 연휴: 9/24(목)~9/26(토)
            Pair(8, 24), Pair(8, 25), Pair(8, 26),
        ),
        2027 to listOf(
            // 설날 연휴: 2/6(토)~2/8(월) + 대체공휴일 2/9(화)
            Pair(1, 6), Pair(1, 7), Pair(1, 8), Pair(1, 9),
            // 부처님오신날: 5/13(목)
            Pair(4, 13),
            // 추석 연휴: 9/14(화)~9/16(목)
            Pair(8, 14), Pair(8, 15), Pair(8, 16),
            // 한글날 대체공휴일: 10/11(월) - 10/9가 토요일
            Pair(9, 11),
        ),
        2028 to listOf(
            // 설날 연휴: 1/25(화)~1/27(목)
            Pair(0, 25), Pair(0, 26), Pair(0, 27),
            // 부처님오신날: 5/2(화)
            Pair(4, 2),
            // 추석 연휴: 10/2(월)~10/4(수) - 10/3 개천절과 겹침
            Pair(9, 2), Pair(9, 3), Pair(9, 4),
            // 추석-개천절 겹침 대체공휴일: 10/5(목)
            Pair(9, 5),
        ),
        2029 to listOf(
            // 설날 연휴: 2/12(월)~2/14(수)
            Pair(1, 12), Pair(1, 13), Pair(1, 14),
            // 부처님오신날: 5/20(일) + 대체공휴일 5/21(월)
            Pair(4, 20), Pair(4, 21),
            // 추석 연휴: 9/21(금)~9/23(일) + 대체공휴일 9/24(월)
            Pair(8, 21), Pair(8, 22), Pair(8, 23), Pair(8, 24),
        ),
        2030 to listOf(
            // 설날 연휴: 2/2(토)~2/4(월) + 대체공휴일 2/5(화)
            Pair(1, 2), Pair(1, 3), Pair(1, 4), Pair(1, 5),
            // 부처님오신날: 5/9(목)
            Pair(4, 9),
            // 추석 연휴: 9/11(수)~9/13(금)
            Pair(8, 11), Pair(8, 12), Pair(8, 13),
        ),
    )

    /**
     * 고정 공휴일 (매년 동일, 월은 0-indexed)
     */
    private val fixedHolidays: List<Pair<Int, Int>> = listOf(
        Pair(0, 1),   // 신정 1/1
        Pair(2, 1),   // 삼일절 3/1
        Pair(4, 5),   // 어린이날 5/5
        Pair(5, 6),   // 현충일 6/6
        Pair(7, 15),  // 광복절 8/15
        Pair(9, 3),   // 개천절 10/3
        Pair(9, 9),   // 한글날 10/9
        Pair(11, 25), // 크리스마스 12/25
    )

    /**
     * 주말 또는 공휴일 여부 확인
     */
    fun isWeekendOrHoliday(cal: Calendar): Boolean {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return true

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // 고정 공휴일 확인
        if (fixedHolidays.any { it.first == month && it.second == day }) return true

        // 변동 공휴일 확인 (해당 연도 데이터가 있는 경우)
        val yearHolidays = variableHolidays[year] ?: return false
        return yearHolidays.any { it.first == month && it.second == day }
    }
}
