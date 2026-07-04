package com.dukkan.payment

/**
 * Terminal result emitted by the payment feature when the payment flow finishes.
 * This is the public contract that crosses the feature-payment module boundary.
 */
sealed interface PaymentResult {
    data class Success(
        val orderId: String,
        val total: Double,
        val currency: String,
        val paymentMethod: String
    ) : PaymentResult

    data class Pending(
        val orderId: String,
    ) : PaymentResult

    data class Failed(
        val error: String,
    ) : PaymentResult

    object Cancelled : PaymentResult
}
