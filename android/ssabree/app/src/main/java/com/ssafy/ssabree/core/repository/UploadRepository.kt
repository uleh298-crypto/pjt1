package com.ssafy.ssabree.core.repository

import java.io.File

interface UploadRepository {
    suspend fun uploadImage(file: File): Result<String>
}
