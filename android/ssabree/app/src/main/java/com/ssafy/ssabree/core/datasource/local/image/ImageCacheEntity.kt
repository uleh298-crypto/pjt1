package com.ssafy.ssabree.core.datasource.local.image

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_image")
data class ImageCacheEntity(
    @PrimaryKey val url: String,
    val data: ByteArray,
    val sizeBytes: Long,
    val lastAccessed: Long
)
