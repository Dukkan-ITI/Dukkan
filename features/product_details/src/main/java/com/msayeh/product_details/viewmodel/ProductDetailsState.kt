package com.msayeh.product_details.viewmodel

import com.msayeh.domain.model.Product

data class ProductDetailsState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val error: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val isAddingToCart: Boolean = false
)
