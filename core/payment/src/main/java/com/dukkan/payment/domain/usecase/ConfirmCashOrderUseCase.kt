package com.dukkan.payment.domain.usecase

import com.msayeh.domain.model.OrderConfirmation
import com.dukkan.payment.domain.repository.PaymentRepository
import com.dukkan.payment.domain.model.CheckoutAddress
import javax.inject.Inject

internal class ConfirmCashOrderUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.msayeh.domain.model.Money,
    ): Result<OrderConfirmation> = repository.confirmCashOrder(
        idempotencyKey = idempotencyKey,
        address        = address,
        cartId         = cartId,
        cartTotal      = cartTotal,
    )
}
