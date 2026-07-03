package com.msayeh.domain.usecase.cart

import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class RemoveDiscountCodeUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(discountCode: String): Result<Unit> {
        return repository.removeDiscountCode(discountCode)
    }
}