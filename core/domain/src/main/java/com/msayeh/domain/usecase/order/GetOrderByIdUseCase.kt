package com.msayeh.domain.usecase.order


import com.msayeh.domain.model.orders.Order
import com.msayeh.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return repository.getOrderById(orderId)
    }
}