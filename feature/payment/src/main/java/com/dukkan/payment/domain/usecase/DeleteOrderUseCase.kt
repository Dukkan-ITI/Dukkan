package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.repository.OrderRepository
import javax.inject.Inject

internal class DeleteOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String): Result<Unit> =
        repository.deleteOrder(orderId)
}
