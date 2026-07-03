package com.dukkan.domain.model

data class FavoriteProduct(
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: String,
    val currencyCode: String
)
