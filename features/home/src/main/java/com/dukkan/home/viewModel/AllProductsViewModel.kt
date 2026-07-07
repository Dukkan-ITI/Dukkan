package com.dukkan.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsUseCase
import com.dukkan.home.R
import com.dukkan.home.uistate.AllProductsUiState
import com.dukkan.home.uistate.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllProductsViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    getFavorites: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<UiText?>(null)

    private companion object {
        const val DEFAULT_PRODUCTS_LIMIT = 50
    }

    val uiState: StateFlow<AllProductsUiState> = combine(
        _products,
        _isLoading,
        _error,
        getFavorites()
    ) { products, isLoading, error, favorites ->
        val favoriteIds = favorites.map { it.id }.toSet()
        when {
            isLoading && products.isEmpty() -> AllProductsUiState.Loading
            error != null && products.isEmpty() -> AllProductsUiState.Error(error)
            else -> AllProductsUiState.Success(
                products = products,
                favoriteIds = favoriteIds
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AllProductsUiState.Loading
    )

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _products.value = getProductsUseCase(limit = DEFAULT_PRODUCTS_LIMIT)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = UiText.StringResource(R.string.error_loading_products)
                _isLoading.value = false
            }
        }
    }

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val favoriteProduct = FavoriteProduct(
                id = product.id,
                title = product.title,
                imageUrl = product.featuredImage?.url ?: "",
                price = product.minPrice.amount.toString(),
                currencyCode = product.minPrice.currencyCode,
                rating = product.averageRating,
                reviewCount = product.reviews.size.takeIf { it > 0 }
            )
            toggleFavoriteUseCase(favoriteProduct, isCurrentlyFavorite)
        }
    }
}
