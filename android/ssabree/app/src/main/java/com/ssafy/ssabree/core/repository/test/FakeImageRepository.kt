package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.ImageRepository

class FakeImageRepository : ImageRepository {
    private val memory = mutableMapOf<String, ByteArray>()

    override suspend fun load(url: String): Result<ByteArray> {
        return memory[url]?.let { Result.success(it) }
            ?: Result.failure(Exception("No cached image in fake repo for $url"))
    }
}
