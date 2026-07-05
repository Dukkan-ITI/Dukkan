package com.dukkan.domain.repository

import com.dukkan.domain.model.cart.StoreCart

interface CartRepository {
    suspend fun getCart(): StoreCart?
    suspend fun addCartItem(variantId: String)
    suspend fun updateCartItemQuantity(lineId: String, quantity: Int)
    suspend fun removeCartItem(lineId: String)
    suspend fun createCart(customerAccessToken: String? = null): String?
    suspend fun saveCartId(cartId: String)
    suspend fun applyDiscountCode(discountCode: String): Result<Unit>
    suspend fun removeDiscountCode(discountCode: String): Result<Unit>
    suspend fun syncCartOnLogin(userId: String)
    suspend fun clearLocalCart()
    suspend fun clearCartFully()
}