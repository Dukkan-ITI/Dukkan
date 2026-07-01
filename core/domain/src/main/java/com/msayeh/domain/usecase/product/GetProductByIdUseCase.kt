package com.msayeh.domain.usecase.product

import com.msayeh.domain.model.Product
import com.msayeh.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productsRepository: ProductsRepository,
) {
    suspend operator fun invoke(productId: String): Result<Product> =
        productsRepository.getProductById(productId)
}