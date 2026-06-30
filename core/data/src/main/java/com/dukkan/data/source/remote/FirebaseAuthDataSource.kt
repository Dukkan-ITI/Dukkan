package com.dukkan.data.source.remote

import com.dukkan.data.mapper.toUserAuthDto
import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

interface FirebaseAuthDataSourceImpl {
    suspend fun loginWithEmailAndPassword(email: String, password: String): UserAuthDto?
    suspend fun registerWithEmailAndPassword(email: String, password: String): UserAuthDto?
    suspend fun loginWithGoogle(idToken: String): UserAuthDto?
    suspend fun getCurrentUser(): UserAuthDto?
    suspend fun signOut()
}

class FirebaseAuthDataSourceImp : FirebaseAuthDataSourceImpl {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): UserAuthDto? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user.toUserAuthDto()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun registerWithEmailAndPassword(
        email: String,
        password: String
    ): UserAuthDto? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user.toUserAuthDto()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun loginWithGoogle(idToken: String): UserAuthDto? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user.toUserAuthDto()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getCurrentUser(): UserAuthDto? {
        return auth.currentUser.toUserAuthDto()
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}