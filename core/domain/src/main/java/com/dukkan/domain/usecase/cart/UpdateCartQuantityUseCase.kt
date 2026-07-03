package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.CartItem
import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity > 0) {
            repository.updateCartItem(cartItem.copy(quantity = newQuantity))
        } else {
            repository.removeCartItem(cartItem.id)
        }
    }
}
