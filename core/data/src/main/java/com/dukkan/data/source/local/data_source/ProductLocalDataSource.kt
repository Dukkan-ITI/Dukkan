package com.dukkan.data.source.local.data_source

import com.dukkan.data.source.local.dao.ProductDao
import com.dukkan.data.source.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductLocalDataSource {
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun insertProducts(products: List<ProductEntity>)
    suspend fun getProductById(id: String): ProductEntity?
    suspend fun deleteProductById(id: String)
    suspend fun clearAll()
}