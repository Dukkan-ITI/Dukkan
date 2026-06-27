package com.dukkan.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dukkan.data.source.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("DELETE FROM products")
    suspend fun clearAll()


}