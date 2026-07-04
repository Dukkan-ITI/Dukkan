package com.dukkan.categories.uiState

import com.dukkan.domain.model.Product

sealed interface CategoryProductsUiState {
    object Loading : CategoryProductsUiState
    data class Error(val message: String) : CategoryProductsUiState
    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>
    ) : CategoryProductsUiState
}