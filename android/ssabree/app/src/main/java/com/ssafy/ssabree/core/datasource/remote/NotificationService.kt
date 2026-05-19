package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.FcmTokenRequest
import com.ssafy.ssabree.core.datasource.remote.model.NotificationResponse
import com.ssafy.ssabree.core.datasource.remote.model.NotificationSettingRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationService {
    @GET("/api/notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @PUT("/api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long)

    @POST("/api/notifications/token")
    suspend fun registerToken(@Body request: FcmTokenRequest)

    @POST("/api/notifications/subscribe")
    suspend fun subscribe(@Body request: FcmTokenRequest)

    @POST("/api/notifications/unsubscribe")
    suspend fun unsubscribe(@Body request: FcmTokenRequest)

    @POST("/api/notifications/settings")
    suspend fun updateSetting(@Body request: NotificationSettingRequest)
}
