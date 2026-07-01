package com.dukkan.data.source.local.data_source.cart

import com.dukkan.data.source.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

interface CartLocalDataSource {
    fun getAllCartItems(): Flow<List<CartEntity>>
    suspend fun addCartItem(cartItem: CartEntity)
    suspend fun updateCartItem(cartItem: CartEntity)
    suspend fun removeCartItem(id: String)
    suspend fun getCartItemById(id: String): CartEntity?
}
