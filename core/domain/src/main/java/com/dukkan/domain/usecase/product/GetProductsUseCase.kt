package com.dukkan.domain.usecase.product

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productsRepository: ProductsRepository
) {
    suspend operator fun invoke(limit: Int = 10, after: String? = null): List<Product> {
        return productsRepository.getProducts(limit = limit, after = after)
    }
}