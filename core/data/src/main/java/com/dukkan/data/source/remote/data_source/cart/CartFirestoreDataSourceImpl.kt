package com.dukkan.data.source.remote.data_source.cart

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CartFirestoreDataSource {

    companion object {
        private const val COLLECTION_NAME = "carts"
        private const val CART_ID_FIELD = "cartId"
        private const val TAG = "CartFirestoreDataSource"
    }

    override suspend fun saveCartId(cartId: String, userId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(userId)
                .set(mapOf(
                    CART_ID_FIELD to cartId,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            Log.d(TAG, "Cart ID saved to Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cart ID to Firestore", e)
            throw e
        }
    }

    override suspend fun getCartId(userId: String): String? {
        return try {
            val document = firestore.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
            document.getString(CART_ID_FIELD).also {
                Log.d(TAG, "Retrieved cart ID from Firestore: $it")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to retrieve cart ID from Firestore", e)
            null
        }
    }

    override suspend fun deleteCartId(userId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(userId)
                .delete()
                .await()
            Log.d(TAG, "Cart ID deleted from Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete cart ID from Firestore", e)
            throw e
        }
    }
}
