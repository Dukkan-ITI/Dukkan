package com.dukkan.home.uistate

import com.dukkan.domain.model.Brand
import com.dukkan.domain.model.Product

sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>,
        val categories: List<String> = emptyList(),
        val brands: List<Brand> = emptyList(),
        ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}