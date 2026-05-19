package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.StackResponse
import retrofit2.http.GET

interface StackService {
    @GET("/api/stacks")
    suspend fun getStacks(): List<StackResponse>
}
