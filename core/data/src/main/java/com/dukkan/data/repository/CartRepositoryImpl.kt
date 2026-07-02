package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.msayeh.domain.model.cart.StoreCart
import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource
) : CartRepository {

    override suspend fun getCart(): StoreCart? {
        val cartId = localDataSource.getCartId() ?: return null
        val cartResponse = remoteDataSource.getCart(cartId)
        return cartResponse?.toDomainModel()
    }

    override suspend fun addCartItem(variantId: String) {
        var cartId = localDataSource.getCartId()
        if (cartId == null) {
            val createdCart = remoteDataSource.createCart()
            cartId = createdCart?.cart?.id
            cartId?.let { localDataSource.saveCartId(it) }
        }
        
        if (cartId != null) {
            remoteDataSource.addCartItem(cartId, variantId)
        }
    }

    override suspend fun updateCartItemQuantity(lineId: String, quantity: Int) {
        val cartId = localDataSource.getCartId() ?: return
        if (quantity <= 0) {
            remoteDataSource.removeCartItem(cartId, lineId)
        } else {
           remoteDataSource.updateCartItem(cartId, lineId, quantity)
        }
    }

    override suspend fun removeCartItem(lineId: String) {
        val cartId = localDataSource.getCartId() ?: return
        remoteDataSource.removeCartItem(cartId, lineId)
    }

    override suspend fun createCart(customerAccessToken: String?): String? {
        val createdCart = remoteDataSource.createCart(customerAccessToken)
        val cartId = createdCart?.cart?.id
        cartId?.let { localDataSource.saveCartId(it) }
        return cartId
    }

    override suspend fun saveCartId(cartId: String) {
        localDataSource.saveCartId(cartId)
    }

    override suspend fun applyDiscountCode(discountCode: String): Result<Unit> {
        val cartId = localDataSource.getCartId()
            ?: return Result.failure(Exception("No active cart found"))
        val result = remoteDataSource.applyDiscountCode(cartId, discountCode)
            ?: return Result.failure(Exception("Failed to apply discount code"))

        val errors = result.userErrors
        if (errors.isNotEmpty()) {
            return Result.failure(Exception(errors.first().message))
        }

        // Shopify may return no userErrors but mark the code as not applicable
        val appliedCode = result.cart?.discountCodes
            ?.firstOrNull { it.code.equals(discountCode, ignoreCase = true) }

        return if (appliedCode?.applicable == false) {
            Result.failure(Exception("Promo code \"$discountCode\" is not applicable to this cart"))
        } else {
            Result.success(Unit)
        }
    }
}
