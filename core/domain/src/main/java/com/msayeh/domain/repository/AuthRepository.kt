package com.msayeh.domain.repository

import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ShopifyToken

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String, firstName: String = "", lastName: String = ""): Result<AuthUser>
    suspend fun loginWithGoogle(idToken: String): Result<AuthUser>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signOut()
    suspend fun getShopifyToken(): ShopifyToken?
}
