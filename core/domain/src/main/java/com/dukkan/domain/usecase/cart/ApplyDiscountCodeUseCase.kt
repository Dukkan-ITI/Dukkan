package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class ApplyDiscountCodeUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(discountCode: String): Result<Unit> {
        return repository.applyDiscountCode(discountCode)
    }
}
