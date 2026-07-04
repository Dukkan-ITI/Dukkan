package com.dukkan.order_list.uistate

import com.msayeh.domain.model.orders.Order

data class OrderHistoryUIState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val error: String? = null,
    val hasNextPage: Boolean = false,
    val endCursor: String? = null
)
