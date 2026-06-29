package com.msayeh.domain.usecase.favorite

import com.msayeh.domain.model.FavoriteProduct

class ToggleFavoriteUseCase(
    private val add: AddFavoriteUseCase,
    private val remove: RemoveFavoriteUseCase
) {
    suspend operator fun invoke(product: FavoriteProduct, isFav: Boolean) {
        if (isFav) remove(product.id) else add(product)
    }
}