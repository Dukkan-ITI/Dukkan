package com.dukkan.domain.usecase.product

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) {
    suspend operator fun invoke(type: String): List<Product> {
        return getProductsUseCase(limit = 250)
            .filter {
                it.productType.equals(type, ignoreCase = true)
            }
    }
}