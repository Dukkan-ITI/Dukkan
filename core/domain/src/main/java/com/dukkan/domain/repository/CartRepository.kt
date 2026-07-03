package com.dukkan.domain.repository

import com.dukkan.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getAllCartItems(): Flow<List<CartItem>>
    suspend fun addCartItem(cartItem: CartItem)
    suspend fun updateCartItem(cartItem: CartItem)
    suspend fun removeCartItem(id: String)
    suspend fun getCartItemById(id: String): CartItem?
}
