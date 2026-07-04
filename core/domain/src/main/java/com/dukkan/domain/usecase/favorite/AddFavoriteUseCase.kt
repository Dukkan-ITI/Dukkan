package com.dukkan.domain.usecase.favorite
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.repository.FavoriteRepository
import javax.inject.Inject
class AddFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    suspend operator fun invoke(product: FavoriteProduct) =
        repository.addFavorite(product)
}
