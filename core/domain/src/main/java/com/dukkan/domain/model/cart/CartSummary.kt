package com.msayeh.domain.model.cart

import com.msayeh.domain.model.Money

/**
 * Lightweight read-only view of a cart passed to the payment feature.
 * Derived from [StoreCart] by the caller before entering the payment flow.
 */
data class CartSummary(
    val cartId: String,
    val total: Money,
    val lineCount: Int,
)
