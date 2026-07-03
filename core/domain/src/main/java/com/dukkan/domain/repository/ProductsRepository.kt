package com.dukkan.domain.repository

import com.dukkan.domain.model.Money
import com.dukkan.domain.model.Product

interface ProductsRepository {
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProducts(limit: Int = 10, after: String? = null): List<Product>
    suspend fun getVariantPrices(variantIds: List<String>): Result<Map<String, Money>>
}