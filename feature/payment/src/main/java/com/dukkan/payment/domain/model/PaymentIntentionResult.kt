package com.dukkan.payment.domain.model

internal data class PaymentIntentionResult(
    val orderId: String,
    val clientSecret: String,
    val publicKey: String,
)
