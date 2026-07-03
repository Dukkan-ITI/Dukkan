package com.msayeh.domain.usecase.cart

import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class SaveCartIdUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(cartId: String) {
        repository.saveCartId(cartId)
    }
}
