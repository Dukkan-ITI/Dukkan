package com.dukkan.domain.model

data class FavoriteProduct(
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: String,
    val currencyCode: String,
    val rating: Float? = null,
    val reviewCount: Int? = null
) {
    fun toMoney(): Money = Money.from(price, currencyCode)
}
