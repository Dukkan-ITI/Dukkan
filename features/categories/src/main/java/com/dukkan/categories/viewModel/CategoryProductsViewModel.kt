package com.dukkan.categories.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.categories.uistate.CategoryProductsUiState
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CategoryProductsEvent {
    data object NavigateToFavoritesGuest : CategoryProductsEvent
}

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    getFavorites: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _events = Channel<CategoryProductsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<CategoryProductsUiState> = combine(
        _products,
        _isLoading,
        _error,
        getFavorites()
    ) { products, isLoading, error, favorites ->
        val favoriteIds = favorites.map { it.id }.toSet()
        when {
            isLoading -> CategoryProductsUiState.Loading
            error != null -> CategoryProductsUiState.Error(error)
            else -> CategoryProductsUiState.Success(
                products = products,
                favoriteIds = favoriteIds
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryProductsUiState.Loading
    )

    fun fetchProductsByHandle(handle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _products.value = getProductsByCategoryUseCase(handle)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("CategoryProductsDebug", "Error loading products: ${e.message}", e)
                _error.value = e.localizedMessage ?: "Error loading products"
                _isLoading.value = false
            }
        }
    }

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _events.send(CategoryProductsEvent.NavigateToFavoritesGuest)
                return@launch
            }

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