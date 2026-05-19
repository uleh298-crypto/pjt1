package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.PortfolioResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioUpdateRequest
import com.ssafy.ssabree.core.datasource.remote.model.SolvedacVerifyResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PortfolioService {
    @GET("/api/portfolios/me")
    suspend fun getMyPortfolios(): List<PortfolioResponse>

    @GET("/api/portfolios/{id}")
    suspend fun getPortfolio(@Path("id") id: Long): PortfolioResponse

    @POST("/api/portfolios")
    suspend fun createPortfolio(@Body request: PortfolioCreateRequest): Long

    @PUT("/api/portfolios/{id}")
    suspend fun updatePortfolio(
        @Path("id") id: Long,
        @Body request: PortfolioUpdateRequest
    ): Long

    @GET("/api/portfolios/solvedac/verify")
    suspend fun verifySolvedac(@Query("handle") handle: String): SolvedacVerifyResponse
}
