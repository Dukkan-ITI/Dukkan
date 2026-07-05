package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.payment.domain.repository.PaymentRepository
import javax.inject.Inject

internal class VerifyPaymentStatusUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(orderId: String, cartTotal: com.dukkan.domain.model.Money? = null): Result<OrderConfirmation> =
        repository.verifyPaymentStatus(orderId, cartTotal)
}
