package com.dukkan.brands.uiState

import com.dukkan.domain.model.Brand

sealed interface BrandsUiState {
    data object Loading : BrandsUiState
    data class Success(val brands: List<Brand>) : BrandsUiState
    data class Error(val message: String) : BrandsUiState
}
