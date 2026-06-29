package com.dukkan.favorites.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.favorites.uistate.FavoritesUiState
import com.msayeh.domain.usecase.favorite.GetFavoritesUseCase
import com.msayeh.domain.usecase.favorite.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavorites: GetFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = getFavorites()
        .map { list ->
            if (list.isEmpty()) FavoritesUiState.Empty
            else FavoritesUiState.Success(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading

        )

    fun onUnfav(id: String) {
        viewModelScope.launch {
            removeFavorite(id)
        }
    }
}