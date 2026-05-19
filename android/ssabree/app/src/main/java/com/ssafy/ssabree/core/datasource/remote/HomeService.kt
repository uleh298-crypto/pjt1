package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.HomeResponse
import retrofit2.http.GET


interface HomeService {
    @GET("/api/home")
    suspend fun getHome(): HomeResponse
}
