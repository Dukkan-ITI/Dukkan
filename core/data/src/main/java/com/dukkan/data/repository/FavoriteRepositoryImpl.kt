package com.dukkan.data.repository



import com.dukkan.data.mapper.toDomain
import com.dukkan.data.mapper.toEntity
import com.dukkan.data.source.local.data_source.FavoriteLocalDataSource
import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.repository.FavoriteRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class FavoriteRepositoryImpl(
    private val localDataSource: FavoriteLocalDataSource
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<FavoriteProduct>> =
        localDataSource.getAllFavorites().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(product: FavoriteProduct) =
        localDataSource.addFavorite(product.toEntity())

    override suspend fun removeFavorite(id: String) =
        localDataSource.removeFavorite(id)

    override fun isFavorite(id: String): Flow<Boolean> =
        localDataSource.isFavorite(id)
}