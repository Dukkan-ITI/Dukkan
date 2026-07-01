package com.msayeh.domain.model.cart

import com.msayeh.domain.model.Money

data class CartCost(
    val subtotalAmount: Money,
    val totalAmount: Money,
    val totalTaxAmount: Money?,
    val checkoutChargeAmount: Money
)