package com.dukkan.brands.uiState

import com.dukkan.domain.model.Product

sealed interface BrandProductsUiState {
    object Loading : BrandProductsUiState
    data class Error(val message: String) : BrandProductsUiState
    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>
    ) : BrandProductsUiState
}