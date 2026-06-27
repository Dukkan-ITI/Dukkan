package com.dukkan.data.source.local.data_source

import com.dukkan.data.source.local.dao.ProductDao
import com.dukkan.data.source.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductLocalDataSourceImpl(
    private val dao: ProductDao
) : ProductLocalDataSource {

    override fun getAllProducts(): Flow<List<ProductEntity>> =
        dao.getAllProducts()

    override suspend fun insertProducts(products: List<ProductEntity>) =
        dao.insertProducts(products)

    override suspend fun getProductById(id: String): ProductEntity? =
        dao.getProductById(id)

    override suspend fun deleteProductById(id: String) =
        dao.deleteProductById(id)

    override suspend fun clearAll() =
        dao.clearAll()
}