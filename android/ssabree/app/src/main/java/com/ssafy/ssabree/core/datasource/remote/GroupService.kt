package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.GroupApplicationRequest
import com.ssafy.ssabree.core.datasource.remote.model.GroupCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.GroupDetailResponse
import com.ssafy.ssabree.core.datasource.remote.model.StudyMemberResponse
import com.ssafy.ssabree.core.datasource.remote.model.TeamMemberResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupSummaryResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupUpdateRequest
import com.ssafy.ssabree.core.datasource.remote.model.GroupApplicationResponse
import com.ssafy.ssabree.core.datasource.remote.model.TeamApplicationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupService {
    @GET("/api/studies")
    suspend fun getStudies(
        @Query("campusId") campusId: Long? = null,
        @Query("type") type: String? = null
    ): List<GroupSummaryResponse>

    @GET("/api/studies/{studyId}")
    suspend fun getStudyDetail(@Path("studyId") studyId: Long): GroupDetailResponse

    @GET("/api/studies/me")
    suspend fun getMyStudies(): List<GroupSummaryResponse>

    @POST("/api/studies")
    suspend fun createStudy(@Body request: GroupCreateRequest): GroupSummaryResponse

    @POST("/api/studies/{studyId}/applications")
    suspend fun applyStudy(
        @Path("studyId") studyId: Long,
        @Body request: GroupApplicationRequest
    )

    @GET("/api/teams")
    suspend fun getTeams(
        @Query("campusId") campusId: Long? = null,
        @Query("type") type: String? = null
    ): List<GroupSummaryResponse>

    @GET("/api/teams/{teamId}")
    suspend fun getTeamDetail(@Path("teamId") teamId: Long): GroupDetailResponse

    @GET("/api/teams/{teamId}/members")
    suspend fun getTeamMembers(@Path("teamId") teamId: Long): List<TeamMemberResponse>

    @DELETE("/api/teams/{teamId}/members/{memberId}")
    suspend fun kickTeamMember(
        @Path("teamId") teamId: Long,
        @Path("memberId") memberId: Long
    )

    @PUT("/api/studies/{studyId}")
    suspend fun updateStudy(
        @Path("studyId") studyId: Long,
        @Body request: GroupUpdateRequest
    ): Unit

    @PUT("/api/teams/{teamId}")
    suspend fun updateTeam(
        @Path("teamId") teamId: Long,
        @Body request: GroupUpdateRequest
    ): Unit

    @DELETE("/api/studies/{studyId}")
    suspend fun deleteStudy(@Path("studyId") studyId: Long): Unit

    @DELETE("/api/teams/{teamId}")
    suspend fun deleteTeam(@Path("teamId") teamId: Long): Unit

    @GET("/api/teams/me")
    suspend fun getMyTeams(): List<GroupSummaryResponse>

    @POST("/api/teams")
    suspend fun createTeam(@Body request: GroupCreateRequest): GroupSummaryResponse

    @POST("/api/teams/{teamId}/applications")
    suspend fun applyTeam(
        @Path("teamId") teamId: Long,
        @Body request: GroupApplicationRequest
    )

    @GET("/api/studies/{studyId}/members")
    suspend fun getStudyMembers(@Path("studyId") studyId: Long): List<StudyMemberResponse>

    @DELETE("/api/studies/{studyId}/members/{memberId}")
    suspend fun kickStudyMember(
        @Path("studyId") studyId: Long,
        @Path("memberId") memberId: Long
    )

    @GET("/api/study-applications/me")
    suspend fun getMyStudyApplications(): List<GroupApplicationResponse>

    @GET("/api/team-applications/me")
    suspend fun getMyTeamApplications(): List<TeamApplicationResponse>

    @DELETE("/api/study-applications/{applicationId}")
    suspend fun cancelStudyApplication(@Path("applicationId") applicationId: Long)

    @DELETE("/api/team-applications/{applicationId}")
    suspend fun cancelTeamApplication(@Path("applicationId") applicationId: Long)

    @DELETE("/api/studies/{studyId}/leave")
    suspend fun leaveStudy(@Path("studyId") studyId: Long)

    @DELETE("/api/teams/{teamId}/leave")
    suspend fun leaveTeam(@Path("teamId") teamId: Long)
}
