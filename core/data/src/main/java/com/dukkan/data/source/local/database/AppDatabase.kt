package com.dukkan.data.source.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dukkan.data.source.local.dao.FavoriteDao
import com.dukkan.data.source.local.entity.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dukkan_db"
                ).build().also{instance = it}
            }
        }
    }
}