package com.ssafy.ssabree.core.datasource.local.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageCacheDao {

    @Query("SELECT * FROM cached_image WHERE url = :url LIMIT 1")
    suspend fun get(url: String): ImageCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ImageCacheEntity)

    @Query("UPDATE cached_image SET lastAccessed = :lastAccessed WHERE url = :url")
    suspend fun updateAccessed(url: String, lastAccessed: Long)

    @Query("SELECT COUNT(*) FROM cached_image")
    suspend fun count(): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM cached_image")
    suspend fun totalSize(): Long

    @Query("SELECT url FROM cached_image ORDER BY lastAccessed ASC LIMIT :limit")
    suspend fun oldestUrls(limit: Int): List<String>

    @Query("DELETE FROM cached_image WHERE url IN (:urls)")
    suspend fun deleteByUrls(urls: List<String>)
}
