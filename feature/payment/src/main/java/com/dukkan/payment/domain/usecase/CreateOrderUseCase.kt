package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import com.dukkan.payment.domain.repository.OrderRepository
import javax.inject.Inject

internal class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        draft: OrderDraft,
        status: OrderFinancialStatus,
    ): Result<CreatedOrder> = repository.createOrder(draft, status)
}
