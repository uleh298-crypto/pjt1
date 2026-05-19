package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.CommentCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.CommentResponse
import com.ssafy.ssabree.core.datasource.remote.model.PagedPostResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.PostDetailResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostLikeResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostUpdateRequest
import com.ssafy.ssabree.core.datasource.remote.model.PollResponse
import com.ssafy.ssabree.core.datasource.remote.model.ReplyCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.ReplyResponse
import com.ssafy.ssabree.core.datasource.remote.model.ScrapResponse
import com.ssafy.ssabree.core.datasource.remote.model.VoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {
    @GET("/api/posts")
    suspend fun getPosts(
        @Query("boardId") boardId: Long? = null,
        @Query("keyword") keyword: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): PagedPostResponse

    @GET("/api/posts/hot")
    suspend fun getHotPosts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): PagedPostResponse

    @GET("/api/posts/{id}")
    suspend fun getPost(@Path("id") id: Long): PostDetailResponse

    @POST("/api/posts")
    suspend fun createPost(@Body request: PostCreateRequest): PostResponse

    @PUT("/api/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Long,
        @Body request: PostUpdateRequest
    ): PostResponse

    @DELETE("/api/posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)

    @POST("/api/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CommentCreateRequest
    ): CommentResponse

    @POST("/api/posts/{postId}/comments/{commentId}/replies")
    suspend fun createReply(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Body request: ReplyCreateRequest
    ): ReplyResponse

    @POST("/api/posts/{postId}/poll/vote")
    suspend fun vote(
        @Path("postId") postId: Long,
        @Body request: VoteRequest
    ): PollResponse

    @POST("/api/posts/{postId}/like")
    suspend fun like(@Path("postId") postId: Long): PostLikeResponse

    @DELETE("/api/posts/{postId}/like")
    suspend fun unlike(@Path("postId") postId: Long): PostLikeResponse

    @POST("/api/posts/{postId}/scrap")
    suspend fun scrap(@Path("postId") postId: Long): ScrapResponse

    @DELETE("/api/posts/{postId}/scrap")
    suspend fun unscrap(@Path("postId") postId: Long): ScrapResponse
}
