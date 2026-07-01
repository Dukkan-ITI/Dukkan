package com.dukkan.home.uiState

import com.msayeh.domain.model.Product

sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String> = emptySet(),
        val endCursor: String? = null,
        val hasNextPage: Boolean = false
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}