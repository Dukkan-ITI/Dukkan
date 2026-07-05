package com.dukkan.domain.usecase.category

import com.dukkan.domain.usecase.product.GetProductsUseCase
import javax.inject.Inject

class GetProductTypesUseCase @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) {

    suspend operator fun invoke(): List<String> {
        return getProductsUseCase(limit = 250)
            .mapNotNull { it.productType }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}