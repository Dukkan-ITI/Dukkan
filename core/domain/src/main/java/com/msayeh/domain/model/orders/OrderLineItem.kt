package com.msayeh.domain.model.orders

import com.dukkan.domain.model.Money

data class OrderLineItem(
    val title: String,
    val quantity: Int,
    val totalPrice: Money,
    val variantId: String?,
    val variantTitle: String?,
    val imageUrl: String?
)
