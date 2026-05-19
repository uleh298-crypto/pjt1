package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.UploadRepository
import java.io.File

class FakeUploadRepository : UploadRepository {
    override suspend fun uploadImage(file: File): Result<String> {
        return runCatching { "https://example.com/uploads/${file.name}" }
    }
}
