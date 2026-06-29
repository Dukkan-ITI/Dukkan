package com.msayeh.domain.usecase.favorite

import com.msayeh.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    suspend operator fun invoke(id: String) =
        repository.removeFavorite(id)
}