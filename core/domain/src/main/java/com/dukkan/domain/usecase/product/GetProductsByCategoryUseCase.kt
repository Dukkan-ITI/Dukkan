package com.dukkan.domain.usecase.product

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(handle: String): List<Product> {
        return productsRepository.getProductsByCollectionHandle(
            handle = handle,
            limit = MAX_PRODUCT_LIMIT
        )
    }

    companion object {
        private const val MAX_PRODUCT_LIMIT = 250
    }
}