package com.dukkan.domain.repository

import com.dukkan.domain.model.Product

interface ProductsRepository {
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProducts(limit: Int = 10, after: String? = null): List<Product>

    suspend fun getProductsByCollectionId(
        categoryId: String,
        limit: Int,
        after: String? = null,
    ): List<Product>

    suspend fun getProductsByType(
        type: String,
        limit: Int,
        after: String? = null
    ): List<Product>
}