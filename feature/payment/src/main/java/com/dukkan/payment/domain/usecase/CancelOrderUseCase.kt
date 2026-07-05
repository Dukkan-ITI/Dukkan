package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.OrderCancelReason
import com.dukkan.payment.domain.repository.OrderRepository
import javax.inject.Inject

internal class CancelOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String, reason: OrderCancelReason): Result<Unit> =
        repository.cancelOrder(orderId, reason)
}
