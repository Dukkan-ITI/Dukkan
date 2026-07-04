package com.dukkan.domain.repository

import com.dukkan.domain.model.FavoriteProduct
import kotlinx.coroutines.flow.Flow


interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<FavoriteProduct>>
    suspend fun addFavorite(product: FavoriteProduct)
    suspend fun removeFavorite(id: String)
    fun isFavorite(id: String): Flow<Boolean>
}