package com.dukkan.payment.domain.model

internal data class OrderLineItemDraft(
    val variantId: String,
    val quantity: Int,
)
