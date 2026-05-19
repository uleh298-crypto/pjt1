package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.InquiryCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.InquiryListResponse
import com.ssafy.ssabree.core.datasource.remote.model.SuccessResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InquiryService {
    @GET("/api/inquiries")
    suspend fun getInquiries(): InquiryListResponse

    @POST("/api/inquiries")
    suspend fun createInquiry(@Body request: InquiryCreateRequest): SuccessResponse
}
