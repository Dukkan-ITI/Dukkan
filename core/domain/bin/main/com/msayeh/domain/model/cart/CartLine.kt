package com.dukkan.domain.model.cart

import com.dukkan.domain.model.ProductVariant

data class CartLine(
    val id: String,
    val quantity: Int,
    val cost: CartLineCost,
    val merchandise: ProductVariant
)
