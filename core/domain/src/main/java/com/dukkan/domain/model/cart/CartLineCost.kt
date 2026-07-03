package com.dukkan.domain.model.cart

import com.dukkan.domain.model.Money

data class CartLineCost(
    val totalAmount: Money,
    val amountPerQuantity: Money,
    val compareAtAmountPerQuantity: Money?
)
