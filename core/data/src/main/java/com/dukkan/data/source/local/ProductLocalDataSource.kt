package com.dukkan.data.source.local

import com.dukkan.data.source.local.dao.ProductDao
import com.dukkan.data.source.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductLocalDataSource(private val dao: ProductDao) {

    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAllProducts()

    suspend fun insertProducts(products: List<ProductEntity>) =
        dao.insertProducts(products)

    suspend fun getProductById(id: String): ProductEntity? =
        dao.getProductById(id)

    suspend fun deleteProductById(id: String) =
        dao.deleteProductById(id)

    suspend fun clearAll() = dao.clearAll()
}