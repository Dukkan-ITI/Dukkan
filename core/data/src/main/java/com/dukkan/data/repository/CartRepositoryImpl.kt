package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toEntity
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.domain.model.CartItem
import com.dukkan.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource
) : CartRepository {

    override fun getAllCartItems(): Flow<List<CartItem>> {
        return localDataSource.getAllCartItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addCartItem(cartItem: CartItem) {
        localDataSource.addCartItem(cartItem.toEntity())
    }

    override suspend fun updateCartItem(cartItem: CartItem) {
        localDataSource.updateCartItem(cartItem.toEntity())
    }

    override suspend fun removeCartItem(id: String) {
        localDataSource.removeCartItem(id)
    }

    override suspend fun getCartItemById(id: String): CartItem? {
        return localDataSource.getCartItemById(id)?.toDomainModel()
    }


}
