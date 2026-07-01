package com.msayeh.domain.model.cart

import com.msayeh.domain.model.ProductVariant

data class CartLine(
    val id: String,
    val quantity: Int,
    val cost: CartLineCost,
    val merchandise: ProductVariant
)
