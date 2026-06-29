package com.dukkan.data.repository

import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.msayeh.domain.model.Product
import com.msayeh.domain.repository.ProductsRepository

class ProductsRepositoryImpl(private val productsDataSource: ProductsDataSource) : ProductsRepository {
    override suspend fun getProductById(productId: String): Result<Product> {
        productsDataSource.getProductById(productId).also {
            return if (it != null) {
                Result.success(it)
            } else {
                Result.failure(Exception("Unknown error occurred"))
            }
        }
    }
}