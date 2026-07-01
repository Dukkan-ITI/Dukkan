package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.CartItem
import com.msayeh.domain.repository.CartRepository
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
