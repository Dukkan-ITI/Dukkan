package com.dukkan.data.source.local.data_source.favorites

import com.dukkan.data.source.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

interface FavoriteLocalDataSource {
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    suspend fun addFavorite(favorite: FavoriteEntity)
    suspend fun removeFavorite(id: String)
    fun isFavorite(id: String): Flow<Boolean>
}