package com.dukkan.domain.model

data class CartItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: Money,
    val size: String,
    val quantity: Int
)

fun List<CartItem>.calculateSubtotal(): Money {
    val currency = firstOrNull()?.price?.currencyCode ?: return Money(currencyCode = "USD")
    return fold(Money(currencyCode = currency)) { acc, item -> acc + item.price * item.quantity }
}