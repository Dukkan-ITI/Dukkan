package com.dukkan.domain.repository

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.ShopifyToken

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String): Result<AuthUser>
    suspend fun loginWithGoogle(idToken: String): Result<AuthUser>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signOut()
    suspend fun getShopifyToken(): ShopifyToken?
}
