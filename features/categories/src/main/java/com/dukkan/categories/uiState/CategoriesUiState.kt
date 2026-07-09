package com.dukkan.categories.uiState

import com.dukkan.domain.model.Category.Category

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Success(val categories: List<Category>) : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
}
