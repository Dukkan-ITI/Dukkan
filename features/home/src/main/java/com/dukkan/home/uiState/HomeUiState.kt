package com.dukkan.home.uiState

import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.model.Product

sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>,
        val categories: List<Category> = emptyList(),
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
