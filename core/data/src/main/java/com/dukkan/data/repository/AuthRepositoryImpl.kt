package com.dukkan.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.FirebaseAuthDataSource
import com.dukkan.data.source.remote.IFirebaseStoreDataSource
import com.dukkan.data.source.remote.ShopifyAuthDataSource
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ShopifyToken
import com.msayeh.domain.repository.AuthRepository
import java.time.Instant
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseStoreDataSource: IFirebaseStoreDataSource,
    private val shopifyAuthDataSource: ShopifyAuthDataSource,
    private val shopifyTokenStore: ShopifyTokenStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val user = firebaseAuthDataSource.loginWithEmailAndPassword(email, password)
            fetchAndStoreShopifyToken(email, password)
            Result.success(user.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<AuthUser> {
        return try {
            val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            val user = firebaseAuthDataSource.registerWithEmailAndPassword(email, password, fullName)

            try {
                firebaseStoreDataSource.saveUser(user)
            } catch (e: Exception) {
                Log.w("AuthRepo", "Firestore saveUser failed (non-fatal): ${e.message}")
            }

            val shopifyCreated = shopifyAuthDataSource.createShopifyCustomer(email, password, firstName, lastName)
            Log.d("AuthRepo", "Shopify customer created: $shopifyCreated")

            fetchAndStoreShopifyToken(email, password)

            Result.success(user.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val user = firebaseAuthDataSource.loginWithGoogle(idToken)
                ?: return Result.failure(Exception("Google login failed"))

            try {
                firebaseStoreDataSource.saveUser(user)
            } catch (e: Exception) {
                Log.w("AuthRepo", "Firestore saveUser failed (non-fatal): ${e.message}")
            }

            val syntheticPassword = "GAuth_${user.uid}!"
            
            var shopifyToken = shopifyAuthDataSource.createCustomerToken(user.email, syntheticPassword)
            
            if (shopifyToken == null) {
                val firstName = user.name.substringBefore(" ").takeIf { it.isNotBlank() } ?: user.email.substringBefore("@")
                val lastName = user.name.substringAfter(" ", "User").takeIf { it.isNotBlank() } ?: "User"
                shopifyAuthDataSource.createShopifyCustomer(user.email, syntheticPassword, firstName, lastName)
                shopifyToken = shopifyAuthDataSource.createCustomerToken(user.email, syntheticPassword)
            }
            
            if (shopifyToken != null) {
                shopifyTokenStore.saveToken(shopifyToken)
                Log.d("AuthRepo", "Shopify token stored via synthetic password. Token: ${shopifyToken.accessToken}")
            } else {
                Log.w("AuthRepo", "Failed to get Shopify token for Google user. (They may have registered previously with an email/password)")
            }

            Result.success(user.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkShopifyViaMultipass(multipassToken: String) {
        val token = shopifyAuthDataSource.createCustomerTokenWithMultipass(multipassToken)
        if (token != null) {
            shopifyTokenStore.saveToken(token)
            Log.d("AuthRepo", "Shopify token stored via Multipass. Token is: ${token.accessToken}")
        } else {
            Log.w("AuthRepo", "Multipass token exchange failed (token is null)")
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return firebaseAuthDataSource.getCurrentUser()?.toDomainModel()
    }

    override suspend fun signOut() {
        firebaseAuthDataSource.signOut()
        shopifyTokenStore.clearToken()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getShopifyToken(): ShopifyToken? {
        val stored = shopifyTokenStore.getToken() ?: return null
        return try {
            val expiry = Instant.parse(stored.expiresAt)
            if (Instant.now().plusSeconds(300).isAfter(expiry)) {
                val renewed = shopifyAuthDataSource.renewCustomerToken(stored.accessToken)
                if (renewed != null) {
                    shopifyTokenStore.saveToken(renewed)
                    renewed.toDomainModel()
                } else {
                    stored.toDomainModel()
                }
            } else {
                stored.toDomainModel()
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error parsing token expiry: ${e.message}")
            stored.toDomainModel()
        }
    }

    private suspend fun fetchAndStoreShopifyToken(email: String, password: String) {
        try {
            val token = shopifyAuthDataSource.createCustomerToken(email, password)
            if (token != null) {
                shopifyTokenStore.saveToken(token)
                Log.d("AuthRepo", "Shopify token stored successfully. Token is: ${token.accessToken}")
            } else {
                Log.w("AuthRepo", "Shopify token was null — not stored")
            }
        } catch (e: Exception) {
            Log.w("AuthRepo", "fetchAndStoreShopifyToken failed (non-fatal): ${e.message}")
        }
    }
}
