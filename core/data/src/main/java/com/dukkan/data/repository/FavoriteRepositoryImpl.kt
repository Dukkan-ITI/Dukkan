package com.dukkan.data.repository


import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toEntity
import com.dukkan.data.source.local.data_source.favorites.FavoriteLocalDataSource
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.repository.FavoriteRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class FavoriteRepositoryImpl @Inject constructor(
    private val localDataSource: FavoriteLocalDataSource
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<FavoriteProduct>> =
        localDataSource.getAllFavorites().map { list -> list.map { it.toDomainModel() } }

    override suspend fun addFavorite(product: FavoriteProduct) =
        localDataSource.addFavorite(product.toEntity())

    override suspend fun removeFavorite(id: String) =
        localDataSource.removeFavorite(id)

    override fun isFavorite(id: String): Flow<Boolean> =
        localDataSource.isFavorite(id)
}