package com.dukkan.domain.usecase.product

import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productsRepository: ProductsRepository,
) {
    suspend operator fun invoke(productId: String): Result<Product> =
        productsRepository.getProductById(productId)
}