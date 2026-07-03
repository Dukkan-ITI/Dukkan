package com.dukkan.data.source.local.data_source.cart

interface CartLocalDataSource {
    suspend fun saveCartId(cartId: String)
    suspend fun getCartId(): String?
    suspend fun deleteCartId()
}
