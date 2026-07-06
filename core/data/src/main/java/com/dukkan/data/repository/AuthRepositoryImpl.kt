package com.dukkan.data.repository

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.GetCustomerOrdersQuery
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.FirebaseAuthDataSource
import com.dukkan.data.source.remote.IFirebaseStoreDataSource
import com.dukkan.data.source.remote.ShopifyAuthDataSource
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.ShopifyToken
import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseStoreDataSource: IFirebaseStoreDataSource,
    private val shopifyAuthDataSource: ShopifyAuthDataSource,
    private val shopifyTokenStore: ShopifyTokenStore,
    private val apolloClient: ApolloClient,
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
            }

            val shopifyCreated = shopifyAuthDataSource.createShopifyCustomer(email, password, firstName, lastName)
            if (!shopifyCreated) {
                Log.w(TAG, "Shopify customer creation returned false for $email")
            }

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
                Log.d(TAG, "Google login — Shopify access token saved: ${shopifyToken.accessToken}")
            } else {
                Log.w(TAG, "Google login — failed to obtain Shopify access token")
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
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return firebaseAuthDataSource.getCurrentUser()?.toDomainModel()
    }

    override suspend fun signOut() {
        firebaseAuthDataSource.signOut()
        shopifyTokenStore.clearToken()
    }

    override suspend fun getShopifyToken(): ShopifyToken? {
        val stored = shopifyTokenStore.getToken() ?: return null
        return try {
            // Shopify date format is ISO-8601, but we can't use Instant on API < 26 easily without Desugaring
            // Since minSdk is 24, let's use a more compatible approach or just return the token if it's there.
            // For now, let's simplify and avoid Instant to prevent crashes on API 24/25.
            stored.toDomainModel()
        } catch (e: Exception) {
            stored.toDomainModel()
        }
    }

    override suspend fun getCustomerId(): Result<String> = runCatching {
        val token = shopifyTokenStore.getToken()?.accessToken
            ?: throw IllegalStateException("User not logged in: customer access token is missing.")

        val response = apolloClient.query(
            GetCustomerOrdersQuery(
                customerAccessToken = token,
                first = 1,
                after = Optional.presentIfNotNull(null),
            )
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.firstOrNull()?.message ?: "Unknown GraphQL Error")
        }

        response.data?.customer?.id ?: throw Exception("Customer not found or invalid token")
    }

    private suspend fun fetchAndStoreShopifyToken(email: String, password: String) {
        try {
            val token = shopifyAuthDataSource.createCustomerToken(email, password)
            if (token != null) {
                shopifyTokenStore.saveToken(token)
                Log.d(TAG, "Login/Register — Shopify access token saved: ${token.accessToken}")
            } else {
                Log.w(TAG, "Login/Register — failed to obtain Shopify access token for $email")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login/Register — error fetching Shopify access token", e)
        }
    }

    private companion object {
        const val TAG = "DukkanAuth"
    }
}
