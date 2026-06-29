package com.msayeh.domain.usecase.favorite

import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(private val repository: FavoriteRepository) {
    operator fun invoke(): Flow<List<FavoriteProduct>> =
        repository.getAllFavorites()
}