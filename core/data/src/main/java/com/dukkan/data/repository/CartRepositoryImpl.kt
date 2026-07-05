package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.dukkan.data.source.remote.data_source.cart.CartFirestoreDataSource
import com.google.firebase.auth.FirebaseAuth
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource,
    private val firestoreDataSource: CartFirestoreDataSource,
    private val tokenStore: ShopifyTokenStore,
    private val firebaseAuth: FirebaseAuth
) : CartRepository {

    private var cachedCart: StoreCart? = null

    private fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "guest_${System.identityHashCode(this)}"
    }

    private suspend fun saveCartIdBoth(cartId: String) {
        localDataSource.saveCartId(cartId)
        try {
            firestoreDataSource.saveCartId(cartId, getCurrentUserId())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save cart ID to Firestore, but local save succeeded", e)
        }
    }

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
                saveCartIdBoth(cartId)
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
        cartId?.let { saveCartIdBoth(it) }
        return cartId
    }

    override suspend fun saveCartId(cartId: String) {
        saveCartIdBoth(cartId)
    }

    override suspend fun applyDiscountCode(discountCode: String): Result<Unit> {
        cachedCart = null
        val cartId = localDataSource.getCartId()
            ?: return Result.failure(Exception("No active cart found"))


        val existingCodes = cachedGetAppliedCodesOrEmpty(cartId)
        val updatedCodes = (existingCodes + discountCode).distinct()

        val result = remoteDataSource.applyDiscountCodes(cartId, updatedCodes)
            ?: return Result.failure(Exception("Failed to apply discount code"))

        val errors = result.userErrors
        if (errors.isNotEmpty()) {
            return Result.failure(Exception(errors.first().message))
        }

        val appliedCode = result.cart?.discountCodes
            ?.firstOrNull { it.code.equals(discountCode, ignoreCase = true) }

        return if (appliedCode?.applicable == false) {
            Result.failure(Exception("Promo code \"$discountCode\" is not applicable to this cart"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun removeDiscountCode(discountCode: String): Result<Unit> {
        cachedCart = null
        val cartId = localDataSource.getCartId()
            ?: return Result.failure(Exception("No active cart found"))

        val existingCodes = cachedGetAppliedCodesOrEmpty(cartId)
        val remainingCodes = existingCodes.filter { !it.equals(discountCode, ignoreCase = true) }


        val result = remoteDataSource.applyDiscountCodes(cartId, remainingCodes)
            ?: return Result.failure(Exception("Failed to remove discount code"))

        val errors = result.userErrors
        if (errors.isNotEmpty()) {
            return Result.failure(Exception(errors.first().message))
        }

        return Result.success(Unit)
    }

    private suspend fun cachedGetAppliedCodesOrEmpty(cartId: String): List<String> {
        val cart = remoteDataSource.getCart(cartId)
        return cart?.discountCodes?.map { it.code } ?: emptyList()
    }
    override suspend fun syncCartOnLogin(userId: String) {
        cachedCart = null
        try {
            localDataSource.deleteCartId()

            val remoteCartId = firestoreDataSource.getCartId(userId)
            if (remoteCartId != null) {
                localDataSource.saveCartId(remoteCartId)
                Log.d(TAG, "Cart synced from Firestore for user: $userId")
            } else {
                Log.d(TAG, "No existing cart found in Firestore for user: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync cart on login", e)
        }
    }

    override suspend fun clearLocalCart() {
        cachedCart = null
        try {
            localDataSource.deleteCartId()
            Log.d(TAG, "Local cart cleared on logout")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cart on logout", e)
        }
    }

    override suspend fun clearCartFully() {
        val cartId = localDataSource.getCartId()
        if (cartId != null) {
            try {
                val cart = remoteDataSource.getCart(cartId)
                cart?.lines?.edges?.forEach { edge ->
                    remoteDataSource.removeCartItem(cartId, edge.node.id)
                }
                if (cart?.discountCodes?.isNotEmpty() == true) {
                    remoteDataSource.applyDiscountCodes(cartId, emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear Storefront cart items before abandoning", e)
            }
        }

        cachedCart = null
        try {
            localDataSource.deleteCartId()
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                firestoreDataSource.deleteCartId(userId)
                Log.d(TAG, "Local cart and Firestore cart cleared")
            } else {
                Log.d(TAG, "Local cart cleared (guest user)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fully clear cart", e)
        }
    }

    private companion object {
        const val TAG = "DukkanCart"
    }
}