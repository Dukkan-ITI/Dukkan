package com.dukkan.data.source.remote.data_source.cart

interface CartFirestoreDataSource {
    suspend fun saveCartId(cartId: String, userId: String)
    suspend fun getCartId(userId: String): String?
    suspend fun deleteCartId(userId: String)
}
