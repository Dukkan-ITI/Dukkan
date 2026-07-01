package com.msayeh.domain.repository

import com.msayeh.domain.model.Product

interface ProductsRepository {
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProducts(limit: Int = 10, after: String? = null): List<Product>
}