package com.msayeh.domain.usecase.cart

import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(id: String) {
        repository.removeCartItem(id)
    }
}
