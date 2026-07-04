package com.dukkan.domain.usecase.favorite

import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(private val repository: FavoriteRepository) {
    operator fun invoke(): Flow<List<FavoriteProduct>> =
        repository.getAllFavorites()
}