package com.dukkan.domain.usecase

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsByBrandUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(
        vendor: String,
        limit: Int = 20,
        after: String? = null,
    ): List<Product> {
        return productsRepository.getProductsByVendor(
            vendor = vendor,
            limit = limit,
            after = after,
        )
    }
}