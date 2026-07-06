package com.dukkan.data.source.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dukkan.data.source.local.dao.FavoriteDao
import com.dukkan.data.source.local.entity.FavoriteEntity


import com.dukkan.data.source.local.dao.OrderDao
import com.dukkan.data.source.local.entity.OrderEntity


@Database(
    entities = [FavoriteEntity::class, OrderEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
}