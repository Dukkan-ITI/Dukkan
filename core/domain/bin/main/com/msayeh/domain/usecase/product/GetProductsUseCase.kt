package com.msayeh.domain.usecase.product

import com.msayeh.domain.model.Product
import com.msayeh.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(limit: Int = 10, after: String? = null): List<Product> {
        return productsRepository.getProducts(limit = limit, after = after)
    }
}