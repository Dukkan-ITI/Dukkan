package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.dao.CartDao
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.local.entity.CartEntity
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.dukkan.data.source.remote.data_source.cart.CartFirestoreDataSource
import com.dukkan.domain.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.CartRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource,
    private val firestoreDataSource: CartFirestoreDataSource,
    private val cartDao: CartDao,
    private val tokenStore: ShopifyTokenStore,
    private val firebaseAuth: FirebaseAuth,
    private val settingsRepository: SettingsRepository,
    private val gson: Gson
) : CartRepository {

    private var cachedCart: StoreCart? = null
    private var cachedCurrencyCountry: String? = null

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
        val country = settingsRepository.currency.first().countryCode

        Log.d("CartRepo", "Fetching cart for country: $country")

        if (cachedCart != null && cachedCurrencyCountry == country) {
            Log.d("CartRepo", "Returning cached cart with subtotal: ${cachedCart?.cost?.subtotalAmount?.amount}")
            return cachedCart
        }

        val cartId = localDataSource.getCartId() ?: run {
            Log.d("CartRepo", "No cart ID found in local storage")
            return null
        }

        Log.d("CartRepo", "Fetching cart from Shopify for ID: $cartId")

        return try {
            val cartResponse = remoteDataSource.getCart(cartId, country)
            if (cartResponse != null) {
                val domainCart = cartResponse.toDomainModel()
                cachedCart = domainCart
                cachedCurrencyCountry = country
                // Persist to Room for offline access
                cartDao.insertCart(CartEntity.fromDomainModel(domainCart, gson))
                domainCart
            } else {
                // Fallback to Room if Shopify returns null
                loadFromLocalCache()
            }
        } catch (e: Exception) {
            Log.e("CartRepo", "Error fetching from remote, falling back to local cache", e)
            loadFromLocalCache()
        }
    }

    private suspend fun loadFromLocalCache(): StoreCart? {
        val localCart = cartDao.getCart().first()?.toDomainModel(gson)
        if (localCart != null) {
            cachedCart = localCart
            // Note: cachedCurrencyCountry is not set here because we don't know the original country of the cached cart
            // but for offline view it's better than nothing.
        }
        return localCart
    }

    override suspend fun addCartItem(variantId: String) {
        cachedCart = null
        cachedCurrencyCountry = null
        var cartId = localDataSource.getCartId()
        if (cartId == null) {
            val customerAccessToken = tokenStore.getToken()?.accessToken
            Log.d(
                TAG,
                "First add to cart — creating cart (hasAccessToken=${customerAccessToken != null})"
            )
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
            val response = remoteDataSource.addCartItem(cartId, variantId)
            if (response?.userErrors?.isNotEmpty() == true) {
                Log.e(TAG, "Error adding item to Shopify cart: ${response.userErrors.first().message}")
            }
        }
    }

    override suspend fun updateCartItemQuantity(lineId: String, quantity: Int) {
        cachedCart = null
        cachedCurrencyCountry = null
        val cartId = localDataSource.getCartId() ?: return
        if (quantity <= 0) {
            remoteDataSource.removeCartItem(cartId, lineId)
        } else {
            remoteDataSource.updateCartItem(cartId, lineId, quantity)
        }
    }

    override suspend fun removeCartItem(lineId: String) {
        cachedCart = null
        cachedCurrencyCountry = null
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
        val country = settingsRepository.currency.first().countryCode
        val cart = remoteDataSource.getCart(cartId, country)
        return cart?.discountCodes?.map { it.code } ?: emptyList()
    }

    override suspend fun syncCartOnLogin(userId: String) {
        cachedCart = null
        try {
            val localCartId = localDataSource.getCartId()
            val remoteCartId = firestoreDataSource.getCartId(userId)

            if (remoteCartId != null && localCartId != null && remoteCartId != localCartId) {

                localDataSource.saveCartId(remoteCartId)
                Log.d(TAG, "Cart synced from Firestore (overwrote local guest cart) for user: $userId")
            } else if (remoteCartId != null) {
                localDataSource.saveCartId(remoteCartId)
                Log.d(TAG, "Cart synced from Firestore for user: $userId")
            } else if (localCartId != null) {
                firestoreDataSource.saveCartId(localCartId, userId)
                Log.d(TAG, "Local cart uploaded to Firestore for new user: $userId")
            } else {
                Log.d(TAG, "No local or remote cart found for user: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync cart on login", e)
        }
    }

    override suspend fun clearLocalCart() {
        cachedCart = null
        cachedCurrencyCountry = null
        try {
            localDataSource.deleteCartId()
            cartDao.clearCart()
            Log.d(TAG, "Local cart cleared on logout")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cart on logout", e)
        }
    }

    override suspend fun clearCartFully() {
        cachedCart = null
        cachedCurrencyCountry = null
        try {
            localDataSource.deleteCartId()
            cartDao.clearCart()
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                firestoreDataSource.deleteCartId(userId)
                Log.d(TAG, "Cart ID cleared from local and Firestore")
            } else {
                Log.d(TAG, "Local cart ID cleared (guest user)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fully clear cart ID", e)
        }
    }

    private companion object {
        const val TAG = "DukkanCart"
    }
}
