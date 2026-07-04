package com.msayeh.domain.usecase.order


import com.msayeh.domain.model.orders.Order
import com.dukkan.domain.repository.OrderRepository
import javax.inject.Inject

class GetRecentOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(): Result<List<Order>> {
        return repository.getOrders(first = 3).map { it.orders }
    }
}