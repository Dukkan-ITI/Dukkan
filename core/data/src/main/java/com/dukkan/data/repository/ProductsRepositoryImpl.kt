package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.msayeh.domain.model.Product
import com.msayeh.domain.repository.ProductsRepository

class ProductsRepositoryImpl(private val productsDataSource: ProductsDataSource) : ProductsRepository {
    override suspend fun getProductById(productId: String): Result<Product> {
        productsDataSource.getProductById(productId).also {
            return if (it != null) {
                Result.success(it.toDomainModel())
            } else {
                Result.failure(Exception("Unknown error occurred"))
            }
        }
    }

    override suspend fun getProducts(limit: Int, after: String?): List<Product> {
        val response = productsDataSource.getProducts(first = limit, after = after)
        return response?.products?.edges?.mapNotNull { edge ->
            edge.node?.toDomainModel()
        } ?: emptyList()
    }
}