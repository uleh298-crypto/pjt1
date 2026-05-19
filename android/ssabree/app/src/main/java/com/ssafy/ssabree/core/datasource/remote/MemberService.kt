package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.MemberResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyCommentResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyPageResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostResponse
import com.ssafy.ssabree.core.datasource.remote.model.UpdateProfileRequest
import com.ssafy.ssabree.core.datasource.remote.model.UpdateProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT

interface MemberService {
    @GET("/api/members/me")
    suspend fun getMe(): MemberResponse

    @GET("/api/members/{id}")
    suspend fun getMember(@Path("id") id: Long): MemberResponse

    @GET("/api/members/mypage")
    suspend fun getMyPage(): MyPageResponse

    @GET("/api/members/mypage/posts")
    suspend fun getMyPosts(): List<PostResponse>

    @GET("/api/members/mypage/comments")
    suspend fun getMyComments(): List<MyCommentResponse>

    @GET("/api/members/mypage/scraps")
    suspend fun getMyScraps(): List<PostResponse>

    @PUT("/api/members/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse
}
