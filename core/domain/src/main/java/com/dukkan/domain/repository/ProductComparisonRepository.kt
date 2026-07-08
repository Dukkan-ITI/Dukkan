package com.dukkan.domain.repository

import com.dukkan.domain.model.ProductComparisonResult
import com.dukkan.domain.model.Product

interface ProductComparisonRepository {
    suspend fun compareProducts(
        product1: Product,
        product2: Product,
        sessionId: String?,
        answer: String?
    ): Result<ProductComparisonResult>
}
