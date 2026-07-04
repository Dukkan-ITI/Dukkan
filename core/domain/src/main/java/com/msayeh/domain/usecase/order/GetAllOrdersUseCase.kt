package com.msayeh.domain.usecase.order


import com.msayeh.domain.model.orders.OrdersPage
import com.dukkan.domain.repository.OrderRepository
import javax.inject.Inject

class GetAllOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(after: String? = null): Result<OrdersPage> {
        return repository.getOrders(first = 20, after = after)
    }
}