package com.msayeh.domain.model

data class SearchProductVariant(
    val id: String,
    val title: String,
    val availableForSale: Boolean,
    val quantityAvailable: Int?,
    val price: Money
)
