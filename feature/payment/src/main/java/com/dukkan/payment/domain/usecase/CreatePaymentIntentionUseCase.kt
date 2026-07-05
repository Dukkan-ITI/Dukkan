package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.repository.PaymentRepository
import com.dukkan.payment.domain.model.CheckoutAddress
import javax.inject.Inject

internal class CreatePaymentIntentionUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<PaymentIntentionResult> = repository.createPaymentIntention(
        idempotencyKey = idempotencyKey,
        address        = address,
        cartId         = cartId,
        cartTotal      = cartTotal,
    )
}
