package com.dukkan.domain.usecase.product

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(categoryHandle: String, limit: Int = 20, after: String? = null): List<Product> {
        return productsRepository.getProductsByCollectionHandle(
            handle = categoryHandle,
            limit = limit,
            after = after,
        )
    }
}
