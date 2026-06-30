package com.dukkan.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.model.Product
import com.msayeh.domain.usecase.favorite.ToggleFavoriteUseCase
import com.msayeh.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.dukkan.home.uiState.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var endCursor: String? = null
    private var hasNextPage: Boolean = false

    init {
        loadInitialProducts()
    }

    fun loadInitialProducts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val products = getProductsUseCase(limit = 10)
                _uiState.value = HomeUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error loading products")
            }
        }
    }

    fun loadMoreProducts() {
        if (!hasNextPage) return
        viewModelScope.launch {
            try {
                val nextProducts = getProductsUseCase(limit = 10, after = endCursor)
                val currentList = (_uiState.value as? HomeUiState.Success)?.products.orEmpty()
                _uiState.value = HomeUiState.Success(currentList + nextProducts)
            } catch (e: Exception) {
            }
        }
    }

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val favoriteProduct = FavoriteProduct(
                id = product.id,
                title = product.title,
                imageUrl = product.featuredImage?.url.orEmpty(),
                price = product.minPrice.amount.toString(),
                currencyCode = product.minPrice.currencyCode
            )
            toggleFavoriteUseCase(favoriteProduct, isCurrentlyFavorite)
        }
    }
}