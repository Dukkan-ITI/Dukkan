package com.dukkan.domain.model.cart

import com.dukkan.domain.model.Money

data class CartCost(
    val subtotalAmount: Money,
    val totalAmount: Money,
    val totalTaxAmount: Money?,
    val checkoutChargeAmount: Money
)