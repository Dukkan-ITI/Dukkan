package com.dukkan.data.source.local.data_source.cart

import com.dukkan.data.source.local.dao.CartDao
import com.dukkan.data.source.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val dao: CartDao
) : CartLocalDataSource {

    override fun getAllCartItems(): Flow<List<CartEntity>> =
        dao.getAllCartItems()

    override suspend fun addCartItem(cartItem: CartEntity) =
        dao.addCartItem(cartItem)

    override suspend fun updateCartItem(cartItem: CartEntity) =
        dao.updateCartItem(cartItem)

    override suspend fun removeCartItem(id: String) =
        dao.removeCartItem(id)

    override suspend fun getCartItemById(id: String): CartEntity? =
        dao.getCartItemById(id)
}
