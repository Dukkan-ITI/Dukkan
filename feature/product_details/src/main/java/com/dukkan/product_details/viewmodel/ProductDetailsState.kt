package com.dukkan.product_details.viewmodel

import com.dukkan.domain.model.Product

data class ProductDetailsState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val error: String? = null
)
