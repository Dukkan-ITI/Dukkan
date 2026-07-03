package com.dukkan.domain.model

data class CartItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: String,
    val currencyCode: String,
    val size: String,
    val quantity: Int
)
