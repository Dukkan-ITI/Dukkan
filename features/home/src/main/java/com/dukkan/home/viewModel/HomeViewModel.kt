package com.dukkan.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Brand
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.GetBrandsUseCase
import com.dukkan.domain.usecase.GetCurrentUserUseCase
import com.dukkan.domain.usecase.category.GetCategoriesUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import com.dukkan.domain.usecase.settings.GetLanguageUseCase
import com.dukkan.home.uistate.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeEvent {
    data object NavigateToFavoritesGuest : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavorites: GetFavoritesUseCase,
    private val getCurrency: GetCurrencyUseCase,
    private val getLanguage: GetLanguageUseCase,
) : ViewModel() {

    private data class HomeContent(
        val products: List<Product>,
        val categories: List<String>,
        val brands: List<Brand>,
    )

    private val _homeContent = MutableStateFlow(HomeContent(emptyList(), emptyList(), emptyList()))
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _firstName = MutableStateFlow<String?>(null)

    val firstName: StateFlow<String?> = _firstName.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        _homeContent,
        _isLoading,
        _error,
        getFavorites()
    ) { content, isLoading, error, favorites ->
        val favoriteIds = favorites.map { it.id }.toSet()
        when {
            isLoading -> HomeUiState.Loading
            error != null -> HomeUiState.Error(error)
            else -> HomeUiState.Success(
                products = content.products,
                favoriteIds = favoriteIds,
                categories = content.categories,
                brands = content.brands
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    private var endCursor: String? = null
    private var hasNextPage: Boolean = false

    init {
        loadCurrentUser()

        // Reload products whenever the selected currency or language changes so the
        // Shopify @inContext presentment currency / localized content stays in sync.
        viewModelScope.launch {
            combine(getCurrency(), getLanguage()) { currency, language -> currency to language }
                .distinctUntilChanged()
                .collect {
                    endCursor = null
                    hasNextPage = false
                    loadInitialProducts()
                }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()
                _firstName.value = user?.name
            } catch (e: Exception) {
                _firstName.value = null
            }
        }
    }

    fun loadInitialProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = getProductsUseCase(limit = 10)

                val products = result
                val categories = getCategoriesUseCase().first().map { it.name }
                val brands = getBrandsUseCase().firstOrNull() ?: emptyList()

                _homeContent.value = HomeContent(products, categories, brands)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error loading products"
                _isLoading.value = false
            }
        }
    }

    fun loadMoreProducts() {
        if (!hasNextPage || _isLoading.value) return

        viewModelScope.launch {
            try {
                val moreProducts = getProductsUseCase(limit = 10)
                _homeContent.update { current ->
                    current.copy(products = current.products + moreProducts)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _events.send(HomeEvent.NavigateToFavoritesGuest)
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