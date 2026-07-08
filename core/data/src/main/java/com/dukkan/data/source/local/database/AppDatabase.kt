package com.dukkan.data.source.local.database


import androidx.room.Database
import androidx.room.RoomDatabase
import com.dukkan.data.source.local.dao.FavoriteDao
import com.dukkan.data.source.local.dao.HomeDao
import com.dukkan.data.source.local.dao.OrderDao
import com.dukkan.data.source.local.entity.FavoriteEntity
import com.dukkan.data.source.local.entity.HomeBrandEntity
import com.dukkan.data.source.local.entity.HomeCategoryEntity
import com.dukkan.data.source.local.entity.HomeProductEntity
import com.dukkan.data.source.local.entity.OrderEntity


@Database(
    entities = [
        FavoriteEntity::class,
        OrderEntity::class,
        HomeProductEntity::class,
        HomeCategoryEntity::class,
        HomeBrandEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
    abstract fun homeDao(): HomeDao
}
