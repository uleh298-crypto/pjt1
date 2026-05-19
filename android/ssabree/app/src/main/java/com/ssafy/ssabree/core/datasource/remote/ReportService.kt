package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.ReportCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.ReportCreateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportService {
    @POST("/api/reports")
    suspend fun createReport(@Body request: ReportCreateRequest): ReportCreateResponse
}
