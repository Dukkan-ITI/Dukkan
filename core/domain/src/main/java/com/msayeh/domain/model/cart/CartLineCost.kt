package com.msayeh.domain.model.cart

import com.msayeh.domain.model.Money

data class CartLineCost(
    val totalAmount: Money,
    val amountPerQuantity: Money,
    val compareAtAmountPerQuantity: Money?
)
