package com.dukkan.domain.usecase.order


import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return repository.getOrderById(orderId)
    }
}