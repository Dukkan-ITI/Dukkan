package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class RemoveDiscountCodeUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(discountCode: String): Result<Unit> {
        return repository.removeDiscountCode(discountCode)
    }
}