package com.dukkan.domain.usecase.favorite


import com.dukkan.domain.repository.FavoriteRepository
import javax.inject.Inject

class ClearFavoritesOnLogoutUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(userId: String) {
        favoriteRepository.clearFavoritesOnLogout(userId)
    }
}