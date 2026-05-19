package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.ProjectCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.ProjectListResponse
import com.ssafy.ssabree.core.datasource.remote.model.ProjectUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProjectService {
    @GET("/api/projects/portfolio/{portfolioId}")
    suspend fun getProjects(@Path("portfolioId") portfolioId: Long): ProjectListResponse

    @POST("/api/projects")
    suspend fun createProject(@Body request: ProjectCreateRequest): Unit

    @PUT("/api/projects/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: Long,
        @Body request: ProjectUpdateRequest
    ): Unit

    @DELETE("/api/projects/{projectId}")
    suspend fun deleteProject(@Path("projectId") projectId: Long): Unit
}
