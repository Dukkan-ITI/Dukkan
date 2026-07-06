package com.dukkan.home.uistate

import com.dukkan.domain.model.Product

sealed interface AllProductsUiState {
    object Loading : AllProductsUiState

    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>
    ) : AllProductsUiState

    data class Error(val message: String) : AllProductsUiState
}
