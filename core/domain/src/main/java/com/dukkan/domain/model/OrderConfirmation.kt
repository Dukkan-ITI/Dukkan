package com.dukkan.domain.model

import com.dukkan.domain.model.Money

/**
 * Terminal result emitted by the payment feature when an order is finalised.
 * This is the only output type that crosses the feature-payment module boundary.
 */
data class OrderConfirmation(
    val orderId: String,
    val status: String,
    val total: Money,
)
