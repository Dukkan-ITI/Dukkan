package com.dukkan.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.model.FavoriteProduct
import com.msayeh.domain.model.Product
import com.msayeh.domain.usecase.favorite.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    fun onFavoriteClick(product: Product, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val favoriteProduct = FavoriteProduct(
                id = product.id,
                title = product.title,
                imageUrl = product.featuredImage.url,
                price = product.minPrice.amount.toString(),
                currencyCode = product.minPrice.currencyCode
            )
            
            toggleFavoriteUseCase(favoriteProduct, isCurrentlyFavorite)
        }
    }
}