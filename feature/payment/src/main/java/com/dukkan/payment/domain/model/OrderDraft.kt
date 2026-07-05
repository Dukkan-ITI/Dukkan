package com.dukkan.payment.domain.model

import com.dukkan.domain.model.Address

internal data class OrderDraft(
    val customerId: String,
    val lineItems: List<OrderLineItemDraft>,
    val shippingLine: ShippingLineDraft,
    val currency: String,
    val shippingAddress: Address?,
)
