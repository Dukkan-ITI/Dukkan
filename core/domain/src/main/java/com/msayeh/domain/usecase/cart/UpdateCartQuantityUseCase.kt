package com.msayeh.domain.usecase.cart

import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(lineId: String, newQuantity: Int) {
        repository.updateCartItemQuantity(lineId, newQuantity)
    }
}
