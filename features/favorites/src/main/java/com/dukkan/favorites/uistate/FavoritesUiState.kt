package com.dukkan.favorites.uistate

import com.msayeh.domain.model.FavoriteProduct

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val favorites: List<FavoriteProduct>) : FavoritesUiState
}