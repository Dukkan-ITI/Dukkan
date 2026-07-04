package com.dukkan.payment.domain.repository

import com.msayeh.domain.model.OrderConfirmation
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.model.CheckoutAddress

internal interface PaymentRepository {

    suspend fun confirmCashOrder(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.msayeh.domain.model.Money,
    ): Result<OrderConfirmation>

    suspend fun createPaymentIntention(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.msayeh.domain.model.Money,
    ): Result<PaymentIntentionResult>

    suspend fun verifyPaymentStatus(orderId: String, cartTotal: com.msayeh.domain.model.Money? = null): Result<OrderConfirmation>
}
