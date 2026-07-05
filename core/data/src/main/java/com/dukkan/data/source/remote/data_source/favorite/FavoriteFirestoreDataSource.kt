package com.dukkan.data.source.remote.data_source.favorite

interface FavoriteFirestoreDataSource {
    suspend fun addFavoriteProductId(productId: String, userId: String)
    suspend fun getFavoriteProductIds(userId: String): List<String>
    suspend fun removeFavoriteProductId(productId: String, userId: String)
    suspend fun deleteAllFavorites(userId: String)
}
