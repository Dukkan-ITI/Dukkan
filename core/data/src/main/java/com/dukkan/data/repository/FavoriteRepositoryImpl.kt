package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toEntity
import com.dukkan.data.source.local.data_source.favorites.FavoriteLocalDataSource
import com.dukkan.data.source.remote.data_source.favorite.FavoriteFirestoreDataSource
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.repository.FavoriteRepository
import com.dukkan.domain.usecase.product.GetProductByIdUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val localDataSource: FavoriteLocalDataSource,
    private val firestoreDataSource: FavoriteFirestoreDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val getProductByIdUseCase: GetProductByIdUseCase, // ✅ جديد
) : FavoriteRepository {

    private fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "guest_${System.identityHashCode(this)}"
    }

    private suspend fun addFavoriteToFirestore(productId: String) {
        try {
            firestoreDataSource.addFavoriteProductId(productId, getCurrentUserId())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add favorite to Firestore, but local save succeeded", e)
        }
    }

    private suspend fun removeFavoriteFromFirestore(productId: String) {
        try {
            firestoreDataSource.removeFavoriteProductId(productId, getCurrentUserId())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove favorite from Firestore, but local remove succeeded", e)
        }
    }

    override fun getAllFavorites(): Flow<List<FavoriteProduct>> =
        localDataSource.getAllFavorites().map { list -> list.map { it.toDomainModel() } }

    override suspend fun addFavorite(product: FavoriteProduct) {
        localDataSource.addFavorite(product.toEntity())
        addFavoriteToFirestore(product.id)
    }

    override suspend fun removeFavorite(id: String) {
        localDataSource.removeFavorite(id)
        removeFavoriteFromFirestore(id)
    }

    override suspend fun syncFavoritesOnLogin(userId: String) {
        try {

            val localFavorites = localDataSource.getAllFavorites().first()
            localFavorites.forEach { favorite ->
                firestoreDataSource.addFavoriteProductId(favorite.id, userId)
            }

            val remoteProductIds = firestoreDataSource.getFavoriteProductIds(userId)
            val localIds = localFavorites.map { it.id }.toSet()

            remoteProductIds.forEach { productId ->
                if (productId in localIds) return@forEach

                getProductByIdUseCase(productId)
                    .onSuccess { product ->
                        val favoriteProduct = FavoriteProduct(
                            id = product.id,
                            title = product.title,
                            imageUrl = product.featuredImage?.url.orEmpty(),
                            price = product.minPrice.amount.toString(),
                            currencyCode = product.minPrice.currencyCode,
                            rating = product.averageRating,
                            reviewCount = product.reviews.size.takeIf { it > 0 }
                        )
                        localDataSource.addFavorite(favoriteProduct.toEntity())
                    }
                    .onFailure { error ->
                        Log.w(TAG, "Failed to fetch product $productId during sync", error)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync favorites on login", e)
        }
    }

    override suspend fun clearFavoritesOnLogout(userId: String) {
        try {
            val allFavorites = localDataSource.getAllFavorites().first()
            allFavorites.forEach { favorite ->
                localDataSource.removeFavorite(favorite.id)
            }
            Log.d(TAG, "Local favorites cleared on logout")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear local favorites on logout", e)
        }
    }

    override fun isFavorite(id: String): Flow<Boolean> =
        localDataSource.isFavorite(id)

    private companion object {
        const val TAG = "DukkanFavorites"
    }
}
