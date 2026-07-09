package com.dukkan.favorites.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.repository.ReviewRepository
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.RemoveFavoriteUseCase
import com.dukkan.favorites.uistate.FavoritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavorites: GetFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(true) // assume true until loaded to avoid flash
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _ratingOverrides = MutableStateFlow<Map<String, Pair<Float, Int>>>(emptyMap())

    init {
        viewModelScope.launch {
            _isLoggedIn.value = getCurrentUser() != null
        }

        viewModelScope.launch {
            reviewRepository.reviewUpdates.collect { (productId, review) ->
                val state = uiState.value
                if (state is FavoritesUiState.Success) {
                    val favorite = state.favorites.find { it.id == productId }
                    if (favorite != null) {
                        val currentCount = favorite.reviewCount ?: 0
                        val currentTotal = (favorite.rating ?: 0f) * currentCount
                        val newCount = currentCount + 1
                        val newAvg = (currentTotal + review.rating) / newCount

                        _ratingOverrides.update { current ->
                            current + (productId to (newAvg to newCount))
                        }
                    }
                }
            }
        }
    }

    val uiState: StateFlow<FavoritesUiState> = combine(
        getFavorites(),
        _ratingOverrides
    ) { list, overrides ->
        val updatedList = list.map { fav ->
            val override = overrides[fav.id]
            if (override != null) {
                fav.copy(rating = override.first, reviewCount = override.second)
            } else fav
        }
        if (updatedList.isEmpty()) FavoritesUiState.Empty
        else FavoritesUiState.Success(updatedList)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading
        )

    private val _showRemoveDialogForProduct = MutableStateFlow<FavoriteProduct?>(null)
    val showRemoveDialogForProduct: StateFlow<FavoriteProduct?> =
        _showRemoveDialogForProduct.asStateFlow()

    fun showRemoveDialog(product: FavoriteProduct) {
        _showRemoveDialogForProduct.value = product
    }

    fun dismissRemoveDialog() {
        _showRemoveDialogForProduct.value = null
    }

    fun confirmRemove() {
        val product = _showRemoveDialogForProduct.value ?: return
        _showRemoveDialogForProduct.value = null
        viewModelScope.launch {
            try {
                removeFavorite(product.id)
            } catch (e: Exception) {
                // handle error or ignore
            }
        }
    }

    fun onUnfav(id: String) {
        viewModelScope.launch {
            removeFavorite(id)
        }
    }
}