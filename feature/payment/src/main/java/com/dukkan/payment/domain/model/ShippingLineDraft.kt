package com.dukkan.payment.domain.model

import com.dukkan.domain.model.Money

internal data class ShippingLineDraft(
    val title: String,
    val code: String,
    val price: Money,
)
