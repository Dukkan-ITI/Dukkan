package com.msayeh.domain.usecase.favorite
import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.repository.FavoriteRepository
import javax.inject.Inject
class AddFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    suspend operator fun invoke(product: FavoriteProduct) =
        repository.addFavorite(product)
}