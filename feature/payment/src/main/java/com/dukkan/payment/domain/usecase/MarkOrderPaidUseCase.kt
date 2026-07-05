package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.repository.OrderRepository
import javax.inject.Inject

internal class MarkOrderPaidUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String): Result<CreatedOrder> =
        repository.markOrderAsPaid(orderId)
}
