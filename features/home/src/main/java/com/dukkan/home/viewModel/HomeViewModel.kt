package com.dukkan.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Brand
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.GetBrandsUseCase
import com.dukkan.domain.usecase.category.GetCategoriesUseCase
import com.dukkan.domain.usecase.category.GetProductTypesUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import com.dukkan.domain.usecase.settings.GetLanguageUseCase
import com.dukkan.home.uistate.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getProductTypesUseCase: GetProductTypesUseCase,
    getFavorites: GetFavoritesUseCase,
    getCurrency: GetCurrencyUseCase,
    getLanguage: GetLanguageUseCase,
) : ViewModel() {

    private data class HomeContent(
        val products: List<Product>,
        val categories: List<String>
    )

    private val _homeContent = MutableStateFlow(HomeContent(emptyList(), emptyList(), emptyList()))
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

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

    fun loadInitialProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = getProductsUseCase(limit = 10)
                // Assuming GetProductsUseCase returns a wrapper containing products and pagination info
                // If it returns List<Product>, we would need to adjust the UseCase or Repository
                val products = result
                val categories = getProductTypesUseCase()

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