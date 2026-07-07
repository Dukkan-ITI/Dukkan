package com.dukkan.data.source.remote.data_source.auth

import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface IFirebaseStoreDataSource {
    suspend fun saveUser(user: UserAuthDto)
}

class FirebaseStoreDataSourceImp : IFirebaseStoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun saveUser(user: UserAuthDto) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to user.name
        )

        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .await()
    }
}