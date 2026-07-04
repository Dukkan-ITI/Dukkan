package com.msayeh.domain.model.orders

data class OrdersPage(
    val orders: List<Order>,
    val hasNextPage: Boolean,
    val endCursor: String?
)
