package com.ssafy.ssabree.core.repository

import android.util.LruCache
import com.ssafy.ssabree.core.datasource.local.image.ImageCacheDao
import com.ssafy.ssabree.core.datasource.local.image.ImageCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val MEMORY_CACHE_MAX_BYTES = 100 * 1024 * 1024 // 100MB
private const val DB_CACHE_MAX_BYTES = 500 * 1024 * 1024L // 500MB
private const val DB_CACHE_MAX_COUNT = 50

class ImageRepositoryImpl(
    private val dao: ImageCacheDao,
    private val client: OkHttpClient
) : ImageRepository {

    private val memoryCache = object : LruCache<String, ByteArray>(MEMORY_CACHE_MAX_BYTES) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }

    override suspend fun load(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            // 1) 메모리 캐시
            memoryCache.get(url)?.let {
                return@runCatching it
            }

            // 2) 로컬 DB
            dao.get(url)?.let { cached ->
                dao.updateAccessed(url, System.currentTimeMillis())
                memoryCache.put(url, cached.data)
                return@runCatching cached.data
            }

            // 3) 원격
            val data = fetchRemote(url)
            val entity = ImageCacheEntity(
                url = url,
                data = data,
                sizeBytes = data.size.toLong(),
                lastAccessed = System.currentTimeMillis()
            )
            dao.upsert(entity)
            memoryCache.put(url, data)

            trimDatabase()
            data
        }
    }

    private fun fetchRemote(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            return response.body?.bytes() ?: error("Empty body")
        }
    }

    private suspend fun trimDatabase() {
        // 조건을 만족할 때까지 오래된 순서로 삭제
        while (true) {
            val totalSize = dao.totalSize()
            val count = dao.count()
            if (totalSize <= DB_CACHE_MAX_BYTES && count <= DB_CACHE_MAX_COUNT) break

            val removeCount = ((count - DB_CACHE_MAX_COUNT).coerceAtLeast(0) + 1).coerceAtMost(5)
            val urls = dao.oldestUrls(removeCount)
            if (urls.isEmpty()) break
            dao.deleteByUrls(urls)
        }
    }
}
