package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus
import retrofit2.http.GET
import retrofit2.http.Path

interface CampusService {

    @GET("/api/campuses")
    suspend fun getCampuses(): List<Campus>

    @GET("/api/campuses/{id}/classes")
    suspend fun getClasses(@Path("id") campusId: Int): List<Ban>
}
