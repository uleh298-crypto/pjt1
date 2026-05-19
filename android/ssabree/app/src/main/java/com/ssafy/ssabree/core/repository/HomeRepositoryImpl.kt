package com.ssafy.ssabree.core.repository

import android.util.Log
import com.ssafy.ssabree.core.datasource.remote.HomeService
import com.ssafy.ssabree.core.repository.model.HomeModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import retrofit2.HttpException


private const val TAG = "HomeRepositoryImpl"
class HomeRepositoryImpl(
    private val ddayRepository: DdayRepository
): HomeRepository {

    private val homeService = RetrofitClient.instance.create(HomeService::class.java)

    override suspend fun fetchHome(): Result<HomeModel> {
        return runCatching {
            val homeModel = homeService.getHome().toModel()

            // DdayRepository에서 모든 D-day 소스 (원격 + 로컬 + 월급날) 가져오기
            val allDdays = ddayRepository.getAllDdays().getOrNull() ?: homeModel.dDays

            homeModel.copy(dDays = allDdays)
        }.onSuccess {
            Log.d(TAG, "fetchHome: success to load home info: \n boards: ${it.boards}, dDays: ${it.dDays}")
        }
        .onFailure {
            if (it is HttpException) {
                val code = it.code()
                val body = it.response()?.errorBody()?.string()
                Log.d(TAG, "fetchHome: failed to load home info (HTTP $code) body=$body")
            } else {
                Log.d(TAG, "fetchHome: failed to load home info", it)
            }

        }
    }
}
