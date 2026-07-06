package com.dukkan.product_details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dukkan.navigation.Screen
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.GetCurrentUserUseCase
import com.dukkan.domain.usecase.cart.CartUseCases
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductByIdUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import com.dukkan.domain.usecase.settings.GetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getFavorites: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val cartUseCases: CartUseCases,
    private val getCurrentUser: GetCurrentUserUseCase,
    getCurrency: GetCurrencyUseCase,
    getLanguage: GetLanguageUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ProductDetailsState())
    val state = _state.asStateFlow()

    private val productId: String = savedStateHandle.toRoute<Screen.ProductDetail>().productId

    init {

        viewModelScope.launch {
            val user = getCurrentUser()
            _state.update { it.copy(isLoggedIn = user != null) }
        }

        viewModelScope.launch {
            combine(getCurrency(), getLanguage()) { currency, language -> currency to language }
                .distinctUntilChanged()
                .collect { getProductDetails() }
        }

        viewModelScope.launch {
            getFavorites().collect { favorites ->
                val favoriteIds = favorites.map { it.id }.toSet()
                _state.update { it.copy(favoriteIds = favoriteIds) }
            }
        }
    }

    fun getProductDetails() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            getProductByIdUseCase(productId).onSuccess { product ->
                _state.value = _state.value.copy(isLoading = false, product = product)
            }.onFailure { error ->
                _state.value = _state.value.copy(isLoading = false, error = error.message)
            }
        }
    }

    fun toggleFavorite(product: Product, isCurrentlyFavorite: Boolean) {
        if (!_state.value.isLoggedIn) {
            _state.update { it.copy(showGuestDialog = true) }
            return
        }

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

    fun addToCart(variantId: String) {
        if (!_state.value.isLoggedIn) {
            _state.update { it.copy(showGuestDialog = true) }
            return
        }

        viewModelScope.launch {
            // Show loading spinner briefly
            _state.update { it.copy(isAddingToCart = true) }
            kotlinx.coroutines.delay(500) // Brief moment of loading

            // Trigger success alert
            _state.update {
                it.copy(
                    isAddingToCart = false,
                    cartAddedSuccess = true
                )
            }

            // Run the actual network call in background (ensures it finishes even if screen closes)
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                try {
                    cartUseCases.addToCart(variantId)
                } catch (e: Exception) {
                    android.util.Log.e("ProductDetails", "Background add to cart failed", e)
                }
            }

            // Hide success alert after a while
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(cartAddedSuccess = false) }
        }
    }

    fun dismissGuestDialog() {
        _state.update { it.copy(showGuestDialog = false) }
    }
}