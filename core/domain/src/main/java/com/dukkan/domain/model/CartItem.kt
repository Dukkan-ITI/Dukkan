package com.dukkan.domain.model

import java.math.BigDecimal

data class CartItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: Money,
    val size: String,
    val quantity: Int
)

fun List<CartItem>.calculateSubtotal(currencyCode: String): Money =
    Money(
        fold(BigDecimal.ZERO) { acc, item -> acc + item.price.amount * item.quantity.toBigDecimal() },
        currencyCode
    )