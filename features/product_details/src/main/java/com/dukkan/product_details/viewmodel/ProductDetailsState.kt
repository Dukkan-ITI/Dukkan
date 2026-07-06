package com.dukkan.product_details.viewmodel

import com.dukkan.domain.model.Product

data class ProductDetailsState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val isAddingToCart: Boolean = false,
    val cartAddedSuccess: Boolean = false,
    val isLoggedIn: Boolean = false,
    val showGuestDialog: Boolean = false
)
