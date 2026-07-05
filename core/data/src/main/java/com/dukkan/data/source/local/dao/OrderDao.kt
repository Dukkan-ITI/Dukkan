package com.dukkan.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dukkan.data.source.local.entity.OrderEntity

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("SELECT * FROM orders ORDER BY processedAt DESC")
    suspend fun getAllOrders(): List<OrderEntity>

    @Query("SELECT * FROM orders ORDER BY processedAt DESC LIMIT :limit")
    suspend fun getRecentOrders(limit: Int): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}
