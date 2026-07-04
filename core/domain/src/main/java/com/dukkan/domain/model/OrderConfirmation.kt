package com.msayeh.domain.model

import com.msayeh.domain.model.Money

/**
 * Terminal result emitted by the payment feature when an order is finalised.
 * This is the only output type that crosses the feature-payment module boundary.
 */
data class OrderConfirmation(
    val orderId: String,
    val status: String,
    val total: Money,
)
