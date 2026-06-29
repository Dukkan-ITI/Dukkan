package com.msayeh.domain.usecase.favorite

import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.repository.FavoriteRepository

class AddFavoriteUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(product: FavoriteProduct) =
        repository.addFavorite(product)
}