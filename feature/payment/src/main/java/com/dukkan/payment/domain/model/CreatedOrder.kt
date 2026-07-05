package com.dukkan.payment.domain.model

import com.dukkan.domain.model.Money

internal data class CreatedOrder(
    val orderId: String,
    val financialStatus: String,
    val total: Money,
)
