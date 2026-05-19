package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    @Multipart
    @POST("/api/uploads/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ImageUploadResponse
}
