package com.msayeh.domain.usecase.favorite

import com.msayeh.domain.repository.FavoriteRepository

class RemoveFavoriteUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(id: String) =
        repository.removeFavorite(id)
}