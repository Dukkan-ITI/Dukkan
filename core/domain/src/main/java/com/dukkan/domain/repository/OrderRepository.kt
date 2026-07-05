package com.dukkan.domain.repository

import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.model.orders.OrdersPage


interface OrderRepository {
    suspend fun getOrders(
        first: Int,
        after: String? = null
    ): Result<OrdersPage>

    suspend fun getOrderById(orderId: String): Result<Order>

    suspend fun clearLocalOrders()
}

