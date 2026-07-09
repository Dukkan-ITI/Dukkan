package com.dukkan.brands.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.brands.uiState.BrandProductsUiState
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.GetProductsByBrandUseCase
import com.dukkan.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BrandProductsEvent {
    // Add events if needed in the future
}

@HiltViewModel
class BrandProductsViewModel @Inject constructor(
    private val getProductsByBrandUseCase: GetProductsByBrandUseCase,
    getFavorites: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _events = Channel<BrandProductsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    
    private val _showGuestAuthDialog = MutableStateFlow(false)
    val showGuestAuthDialog: StateFlow<Boolean> = _showGuestAuthDialog.asStateFlow()

    fun dismissGuestAuthDialog() {
        _showGuestAuthDialog.value = false
    }

    val uiState: StateFlow<BrandProductsUiState> = combine(
        _products,
        _isLoading,
        _error,
        getFavorites()
    ) { products, isLoading, error, favorites ->
        val favoriteIds = favorites.map { it.id }.toSet()
        when {
            isLoading -> BrandProductsUiState.Loading
            error != null -> BrandProductsUiState.Error(error)
            else -> BrandProductsUiState.Success(
                products = products,
                favoriteIds = favoriteIds
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BrandProductsUiState.Loading
    )

    fun fetchProductsByVendor(vendor: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _products.value = getProductsByBrandUseCase(vendor)
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("BrandProductsDebug", "Error loading products: ${e.message}", e)
                _error.value = e.localizedMessage ?: "Error loading products"
                _isLoading.value = false
            }
        }
    }

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _showGuestAuthDialog.value = true
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