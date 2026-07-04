package com.dukkan.domain.model

data class SearchProduct(
    val id: String,
    val title: String,
    val vendor: String,
    val productType: String = "",
    val availableForSale: Boolean,
    val price: Money,
    val imageUrl: String?,
    val imageAltText: String?,
    val variants: List<SearchProductVariant>
)
