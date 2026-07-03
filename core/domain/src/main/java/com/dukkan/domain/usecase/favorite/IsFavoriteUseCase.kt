package com.dukkan.domain.usecase.favorite

import com.dukkan.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    operator fun invoke(id: String): Flow<Boolean> =
        repository.isFavorite(id)
}