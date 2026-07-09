package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.Address
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.repository.PaymentRepository
import javax.inject.Inject

internal class CreatePaymentIntentionUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(
        idempotencyKey: String,
        billingAddress: Address,
        cartId: String,
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<PaymentIntentionResult> = repository.createPaymentIntention(
        idempotencyKey = idempotencyKey,
        billingAddress = billingAddress,
        cartId         = cartId,
        cartTotal      = cartTotal,
    )
}
