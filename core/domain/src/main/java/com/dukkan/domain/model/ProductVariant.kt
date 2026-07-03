package com.dukkan.domain.model

data class ProductVariant(
    val id: String,
    val title: String,
    val price: Money,
    val image: NetworkImage?,
    val availableForSale: Boolean,
    val quantityAvailable: Int,
)
