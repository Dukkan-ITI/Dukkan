package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.remote.FirebaseAuthDataSourceImpl
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSourceImpl
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val user = authDataSource.loginWithEmailAndPassword(email, password)
            if (user != null) {
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
    }
}
