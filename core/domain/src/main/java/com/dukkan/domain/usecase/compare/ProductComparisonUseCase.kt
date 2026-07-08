package com.dukkan.domain.usecase.compare

import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductComparisonResult
import com.dukkan.domain.repository.ProductComparisonRepository
import javax.inject.Inject

class ProductComparisonUseCase @Inject constructor(
    private val repository: ProductComparisonRepository
) {
    suspend operator fun invoke(
        product1: Product,
        product2: Product,
        sessionId: String? = null,
        answer: String? = null
    ): Result<ProductComparisonResult> {
        return repository.compareProducts(product1, product2, sessionId, answer)
    }
}
