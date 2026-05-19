package com.ssafy.ssabree.core.datasource.local.image

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ImageCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ImageCacheDatabase : RoomDatabase() {
    abstract fun imageCacheDao(): ImageCacheDao

    companion object {
        @Volatile
        private var INSTANCE: ImageCacheDatabase? = null

        fun getInstance(context: Context): ImageCacheDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ImageCacheDatabase::class.java,
                    "image_cache.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
