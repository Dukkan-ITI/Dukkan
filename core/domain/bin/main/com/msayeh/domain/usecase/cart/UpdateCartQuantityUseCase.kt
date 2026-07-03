package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(lineId: String, newQuantity: Int) {
        repository.updateCartItemQuantity(lineId, newQuantity)
    }
}
