package com.ssafy.ssabree.core.repository

import android.util.Log
import com.ssafy.ssabree.core.datasource.local.DdayLocalStore
import com.ssafy.ssabree.core.datasource.remote.DdayService
import com.ssafy.ssabree.core.repository.model.DDayModel
import com.ssafy.ssabree.core.repository.model.DdayItemModel
import com.ssafy.ssabree.core.repository.model.toDdayItemModel
import com.ssafy.ssabree.core.utils.KoreanHolidays
import com.ssafy.ssabree.core.utils.RetrofitClient
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "DdayRepositoryImpl"

class DdayRepositoryImpl(
    private val ddayLocalStore: DdayLocalStore
) : DdayRepository {
    private val ddayService = RetrofitClient.instance.create(DdayService::class.java)

    override suspend fun fetchDdays(): Result<List<DdayItemModel>> {
        return runCatching {
            ddayService.getDdays().items.map { it.toDdayItemModel() }
        }.onSuccess { items ->
            Log.d(TAG, "fetchDdays: success to load dday list (count=${items.size})")
        }.onFailure {
            if (it is HttpException) {
                val code = it.code()
                val body = it.response()?.errorBody()?.string()
                Log.d(TAG, "fetchDdays: failed to load dday list (HTTP $code) body=$body")
            } else {
                Log.d(TAG, "fetchDdays: failed to load dday list", it)
            }
        }
    }

    override suspend fun getAllDdays(): Result<List<DDayModel>> {
        return runCatching {
            val allDdays = mutableListOf<DDayModel>()

            // 1. 원격 D-day 가져오기
            fetchDdays().onSuccess { remoteDdays ->
                remoteDdays.forEach { item ->
                    allDdays.add(DDayModel(title = item.title, days = item.dDay))
                }
            }

            // 2. 로컬 D-day 가져오기 (showOnHome이 true인 것만)
            val localItems = ddayLocalStore.load()
            localItems.filter { it.showOnHome }.forEach { item ->
                val days = calculateDdayDays(item.date)
                if (days >= 0) { // 아직 지나지 않은 D-day만 표시
                    allDdays.add(DDayModel(title = item.title, days = days))
                }
            }

            // 3. 다음 월급날 D-day 계산
            val salaryDday = getNextSalaryDday()
            allDdays.add(DDayModel(title = "월급날", days = salaryDday))

            // D-day가 가까운 순으로 정렬
            allDdays.sortedBy { it.days }
        }.onSuccess { items ->
            Log.d(TAG, "getAllDdays: success (count=${items.size})")
        }.onFailure { e ->
            Log.e(TAG, "getAllDdays: failed (${e.message})")
        }
    }

    /**
     * 다음 월급날까지 남은 일수 계산
     */
    private fun getNextSalaryDday(): Int {
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        // 이번 달 월급날
        val salaryDayThisMonth = getSalaryDay(year, month)

        return if (dayOfMonth <= salaryDayThisMonth) {
            // 이번 달 월급날이 아직 안 지났음
            salaryDayThisMonth - dayOfMonth
        } else {
            // 이번 달 월급날이 지났으면 다음 달 월급날 계산
            val nextMonth = if (month == 11) 0 else month + 1
            val nextYear = if (month == 11) year + 1 else year
            val salaryDayNextMonth = getSalaryDay(nextYear, nextMonth)

            val nextSalaryCal = Calendar.getInstance().apply {
                set(nextYear, nextMonth, salaryDayNextMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = nextSalaryCal.timeInMillis - todayCal.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        }
    }

    /**
     * 특정 월의 월급날 계산 (15일 기준, 주말/공휴일이면 다음 평일)
     */
    private fun getSalaryDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance().apply { set(year, month, 15) }
        while (KoreanHolidays.isWeekendOrHoliday(cal)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 날짜 문자열로부터 D-day 일수 계산
     */
    private fun calculateDdayDays(dateString: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
            val targetDate = Calendar.getInstance().apply {
                time = sdf.parse(dateString) ?: return Int.MAX_VALUE
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = targetDate.timeInMillis - today.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }
}
