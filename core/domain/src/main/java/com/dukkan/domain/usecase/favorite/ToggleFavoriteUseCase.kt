package com.dukkan.domain.usecase.favorite

import com.dukkan.domain.model.FavoriteProduct
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val add: AddFavoriteUseCase,
    private val remove: RemoveFavoriteUseCase
) {
    suspend operator fun invoke (product: FavoriteProduct, isFav: Boolean) {
        if (isFav) remove(product.id) else add(product)
    }
}