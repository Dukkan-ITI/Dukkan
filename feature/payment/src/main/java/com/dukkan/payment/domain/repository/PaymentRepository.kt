package com.dukkan.payment.domain.repository

import com.dukkan.domain.model.Address
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.payment.domain.model.PaymentIntentionResult

internal interface PaymentRepository {

    suspend fun confirmCashOrder(
        idempotencyKey: String,
        billingAddress: Address,
        cartId: String,
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<OrderConfirmation>

    suspend fun createPaymentIntention(
        idempotencyKey: String,
        billingAddress: Address,
        cartId: String,
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<PaymentIntentionResult>

    suspend fun verifyPaymentStatus(orderId: String, cartTotal: com.dukkan.domain.model.Money? = null): Result<OrderConfirmation>
}
