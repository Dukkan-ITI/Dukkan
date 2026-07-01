package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.CartItem
import com.msayeh.domain.repository.CartRepository

import javax.inject.Inject

class AddToCartUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(cartItem: CartItem) {
        val existingItem = repository.getCartItemById(cartItem.id)
        if (existingItem != null) {
            repository.updateCartItem(existingItem.copy(quantity = existingItem.quantity + cartItem.quantity))
        } else {
            repository.addCartItem(cartItem)
        }
    }
}
