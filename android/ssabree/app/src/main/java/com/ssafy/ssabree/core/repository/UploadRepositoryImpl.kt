package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.UploadService
import com.ssafy.ssabree.core.utils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadRepositoryImpl : UploadRepository {

    private val uploadService: UploadService = RetrofitClient.uploadInstance.create(UploadService::class.java)

    override suspend fun uploadImage(file: File): Result<String> {
        return runCatching {
            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val multipart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = requestBody
            )
            uploadService.uploadImage(multipart).url
        }
    }
}
