package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.GroupApplicationResponse
import com.ssafy.ssabree.core.datasource.remote.model.TeamApplicationResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupNoticeRequest
import com.ssafy.ssabree.core.datasource.remote.model.GroupNoticeResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupTaskRequest
import com.ssafy.ssabree.core.datasource.remote.model.GroupTaskResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupTaskStatusRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MyGroupService {
    @GET("/api/studies/{studyId}/applications")
    suspend fun getStudyApplications(@Path("studyId") studyId: Long): List<GroupApplicationResponse>

    @GET("/api/study-applications/{applicationId}")
    suspend fun getStudyApplicationDetail(@Path("applicationId") applicationId: Long): GroupApplicationResponse

    @GET("/api/teams/{teamId}/applications")
    suspend fun getTeamApplications(@Path("teamId") teamId: Long): List<TeamApplicationResponse>

    @GET("/api/team-applications/{applicationId}")
    suspend fun getTeamApplicationDetail(@Path("applicationId") applicationId: Long): TeamApplicationResponse

    @POST("/api/study-applications/{applicationId}/accept")
    suspend fun acceptStudyApplication(@Path("applicationId") applicationId: Long)

    @POST("/api/study-applications/{applicationId}/reject")
    suspend fun rejectStudyApplication(@Path("applicationId") applicationId: Long)

    @POST("/api/team-applications/{applicationId}/accept")
    suspend fun acceptTeamApplication(@Path("applicationId") applicationId: Long)

    @POST("/api/team-applications/{applicationId}/reject")
    suspend fun rejectTeamApplication(@Path("applicationId") applicationId: Long)

    @GET("/api/studies/{studyId}/notices")
    suspend fun getStudyNotices(@Path("studyId") studyId: Long): List<GroupNoticeResponse>

    @GET("/api/teams/{teamId}/notices")
    suspend fun getTeamNotices(@Path("teamId") teamId: Long): List<GroupNoticeResponse>

    @POST("/api/studies/{studyId}/notices")
    suspend fun createStudyNotice(
        @Path("studyId") studyId: Long,
        @Body request: GroupNoticeRequest
    )

    @POST("/api/teams/{teamId}/notices")
    suspend fun createTeamNotice(
        @Path("teamId") teamId: Long,
        @Body request: GroupNoticeRequest
    )

    @PUT("/api/studies/{studyId}/notices/{noticeId}")
    suspend fun updateStudyNotice(
        @Path("studyId") studyId: Long,
        @Path("noticeId") noticeId: Long,
        @Body request: GroupNoticeRequest
    )

    @PUT("/api/teams/{teamId}/notices/{noticeId}")
    suspend fun updateTeamNotice(
        @Path("teamId") teamId: Long,
        @Path("noticeId") noticeId: Long,
        @Body request: GroupNoticeRequest
    )

    @DELETE("/api/studies/{studyId}/notices/{noticeId}")
    suspend fun deleteStudyNotice(
        @Path("studyId") studyId: Long,
        @Path("noticeId") noticeId: Long
    )

    @DELETE("/api/teams/{teamId}/notices/{noticeId}")
    suspend fun deleteTeamNotice(
        @Path("teamId") teamId: Long,
        @Path("noticeId") noticeId: Long
    )

    @GET("/api/studies/{studyId}/tasks")
    suspend fun getStudyTasks(@Path("studyId") studyId: Long): List<GroupTaskResponse>

    @GET("/api/teams/{teamId}/tasks")
    suspend fun getTeamTasks(@Path("teamId") teamId: Long): List<GroupTaskResponse>

    @POST("/api/studies/{studyId}/tasks")
    suspend fun createStudyTask(
        @Path("studyId") studyId: Long,
        @Body request: GroupTaskRequest
    ): GroupTaskResponse

    @POST("/api/teams/{teamId}/tasks")
    suspend fun createTeamTask(
        @Path("teamId") teamId: Long,
        @Body request: GroupTaskRequest
    ): GroupTaskResponse

    @PUT("/api/study-tasks/{taskId}/status")
    suspend fun updateStudyTaskStatus(
        @Path("taskId") taskId: Long,
        @Body request: GroupTaskStatusRequest
    )

    @PUT("/api/team-tasks/{taskId}/status")
    suspend fun updateTeamTaskStatus(
        @Path("taskId") taskId: Long,
        @Body request: GroupTaskStatusRequest
    )

    @PUT("/api/study-tasks/{taskId}")
    suspend fun updateStudyTask(
        @Path("taskId") taskId: Long,
        @Body request: GroupTaskRequest
    )

    @PUT("/api/team-tasks/{taskId}")
    suspend fun updateTeamTask(
        @Path("taskId") taskId: Long,
        @Body request: GroupTaskRequest
    )

    @DELETE("/api/study-tasks/{taskId}")
    suspend fun deleteStudyTask(@Path("taskId") taskId: Long)

    @DELETE("/api/team-tasks/{taskId}")
    suspend fun deleteTeamTask(@Path("taskId") taskId: Long)
}
