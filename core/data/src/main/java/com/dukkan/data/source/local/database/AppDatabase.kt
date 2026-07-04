package com.dukkan.data.source.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dukkan.data.source.local.dao.FavoriteDao
import com.dukkan.data.source.local.entity.FavoriteEntity



@Database(entities = [FavoriteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}