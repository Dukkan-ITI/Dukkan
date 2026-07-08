package com.dukkan.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dukkan.data.source.local.entity.HomeBrandEntity
import com.dukkan.data.source.local.entity.HomeCategoryEntity
import com.dukkan.data.source.local.entity.HomeProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {
    @Query("SELECT * FROM home_products")
    fun getProducts(): Flow<List<HomeProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<HomeProductEntity>)

    @Query("DELETE FROM home_products")
    suspend fun clearProducts()

    @Query("SELECT * FROM home_categories")
    fun getCategories(): Flow<List<HomeCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<HomeCategoryEntity>)

    @Query("DELETE FROM home_categories")
    suspend fun clearCategories()

    @Query("SELECT * FROM home_brands")
    fun getBrands(): Flow<List<HomeBrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<HomeBrandEntity>)

    @Query("DELETE FROM home_brands")
    suspend fun clearBrands()

    @Transaction
    suspend fun updateHomeData(
        products: List<HomeProductEntity>,
        categories: List<HomeCategoryEntity>,
        brands: List<HomeBrandEntity>
    ) {
        clearProducts()
        insertProducts(products)
        clearCategories()
        insertCategories(categories)
        clearBrands()
        insertBrands(brands)
    }
}
