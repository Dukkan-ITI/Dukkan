package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.msayeh.domain.model.cart.StoreCart
import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource,
    private val tokenStore: ShopifyTokenStore
) : CartRepository {

    private var cachedCart: StoreCart? = null

    override suspend fun getCart(): StoreCart? {
        if (cachedCart != null) return cachedCart
        val cartId = localDataSource.getCartId() ?: return null
        val cartResponse = remoteDataSource.getCart(cartId)
        cachedCart = cartResponse?.toDomainModel()
        return cachedCart
    }

    override suspend fun addCartItem(variantId: String) {
        cachedCart = null
        var cartId = localDataSource.getCartId()
        if (cartId == null) {
            val customerAccessToken = tokenStore.getToken()?.accessToken
            Log.d(TAG, "First add to cart — creating cart (hasAccessToken=${customerAccessToken != null})")
            if (customerAccessToken != null) {
                Log.d(TAG, "First add to cart — using customer access token: $customerAccessToken")
            } else {
                Log.w(TAG, "First add to cart — no customer access token, creating guest cart")
            }
            val createdCart = remoteDataSource.createCart(customerAccessToken)
            cartId = createdCart?.cart?.id
            if (cartId != null) {
                localDataSource.saveCartId(cartId)
                Log.d(TAG, "First add to cart — cart created with id: $cartId")
            } else {
                Log.e(TAG, "First add to cart — cart creation failed")
            }
        }
        
        if (cartId != null) {
            remoteDataSource.addCartItem(cartId, variantId)
        }
    }

    override suspend fun updateCartItemQuantity(lineId: String, quantity: Int) {
        cachedCart = null
        val cartId = localDataSource.getCartId() ?: return
        if (quantity <= 0) {
            remoteDataSource.removeCartItem(cartId, lineId)
        } else {
           remoteDataSource.updateCartItem(cartId, lineId, quantity)
        }
    }

    override suspend fun removeCartItem(lineId: String) {
        cachedCart = null
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
        cachedCart = null
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

    private companion object {
        const val TAG = "DukkanCart"
    }
}
