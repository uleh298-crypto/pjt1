package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.DdayListResponse
import retrofit2.http.GET

interface DdayService {
    @GET("/api/ddays")
    suspend fun getDdays(): DdayListResponse
}
