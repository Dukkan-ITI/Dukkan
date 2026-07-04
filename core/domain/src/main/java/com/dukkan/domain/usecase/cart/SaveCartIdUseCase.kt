package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class SaveCartIdUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(cartId: String) {
        repository.saveCartId(cartId)
    }
}
