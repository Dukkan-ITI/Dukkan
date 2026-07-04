package com.dukkan.data.source.local.data_source.favorites

import com.dukkan.data.source.local.dao.FavoriteDao
import com.dukkan.data.source.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteLocalDataSourceImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoriteLocalDataSource {

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> =
        dao.getAllFavorites()

    override suspend fun addFavorite(favorite: FavoriteEntity) =
        dao.addFavorite(favorite)

    override suspend fun removeFavorite(id: String) =
        dao.removeFavorite(id)

    override fun isFavorite(id: String): Flow<Boolean> =
        dao.isFavorite(id)
}
