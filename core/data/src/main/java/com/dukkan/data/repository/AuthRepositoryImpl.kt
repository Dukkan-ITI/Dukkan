package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.FirebaseAuthDataSource
import com.dukkan.data.source.remote.ShopifyAuthDataSource
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ShopifyToken
import com.msayeh.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val shopifyAuthDataSource: ShopifyAuthDataSource,
    private val shopifyTokenStore: ShopifyTokenStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val user = authDataSource.loginWithEmailAndPassword(email, password)
            if (user != null) {
                val shopifyToken = shopifyAuthDataSource.createCustomerToken(email, password)
                if (shopifyToken != null) shopifyTokenStore.saveToken(shopifyToken)
                Result.success(user.toDomainModel())
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> {
        return try {
            val user = authDataSource.registerWithEmailAndPassword(email, password)
            if (user != null) {
                // Best-effort: create and persist Shopify token — never fails the register result
                val shopifyToken = shopifyAuthDataSource.createCustomerToken(email, password)
                if (shopifyToken != null) shopifyTokenStore.saveToken(shopifyToken)
                Result.success(user.toDomainModel())
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val user = authDataSource.loginWithGoogle(idToken)
            if (user != null) {
                Result.success(user.toDomainModel())
            } else {
                Result.failure(Exception("Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return authDataSource.getCurrentUser()?.toDomainModel()
    }

    override suspend fun signOut() {
        authDataSource.signOut()
        shopifyTokenStore.clearToken()
    }

    override suspend fun getShopifyToken(): ShopifyToken? {
        return shopifyTokenStore.getToken()?.toDomainModel()
    }
}
