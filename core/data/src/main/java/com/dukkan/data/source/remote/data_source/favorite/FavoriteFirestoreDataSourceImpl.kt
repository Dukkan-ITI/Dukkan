package com.dukkan.data.source.remote.data_source.favorite

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FavoriteFirestoreDataSource {

    companion object {
        private const val COLLECTION_NAME = "favorites"
        private const val PRODUCT_IDS_FIELD = "productIds"
        private const val TAG = "FavoriteFirestoreDataSource"
    }

    override suspend fun addFavoriteProductId(productId: String, userId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(userId)
                .set(
                    mapOf(
                        PRODUCT_IDS_FIELD to FieldValue.arrayUnion(productId),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Log.d(TAG, "Favorite product ID added to Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add favorite product ID to Firestore", e)
            throw e
        }
    }

    override suspend fun getFavoriteProductIds(userId: String): List<String> {
        return try {
            val document = firestore.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
            @Suppress("UNCHECKED_CAST")
            ((document.get(PRODUCT_IDS_FIELD) as? List<String>) ?: emptyList()).also {
                Log.d(TAG, "Retrieved favorite product IDs from Firestore: $it")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to retrieve favorite product IDs from Firestore", e)
            emptyList()
        }
    }

    override suspend fun removeFavoriteProductId(productId: String, userId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(userId)
                .update(
                    mapOf(
                        PRODUCT_IDS_FIELD to FieldValue.arrayRemove(productId),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Log.d(TAG, "Favorite product ID removed from Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove favorite product ID from Firestore", e)
            throw e
        }
    }

    override suspend fun deleteAllFavorites(userId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(userId)
                .delete()
                .await()
            Log.d(TAG, "All favorites deleted from Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete all favorites from Firestore", e)
            throw e
        }
    }
}
