package com.dukkan.domain.model.orders

import com.dukkan.domain.model.Money


data class Order(
    val id: String,
    val orderNumber: Int,
    val processedAt: String,
    val financialStatus: String?,
    val fulfillmentStatus: String?,
    val totalPrice: Money,
    val subtotalPrice: Money,
    val lineItems: List<OrderLineItem>,
    val shippingAddress: ShippingAddress?
) {
    val displayStatus: OrderDisplayStatus
        get() = when {
            financialStatus == "REFUNDED" || financialStatus == "VOIDED" -> OrderDisplayStatus.CANCELLED
            financialStatus == "PENDING" -> OrderDisplayStatus.PENDING
            fulfillmentStatus == "FULFILLED" -> OrderDisplayStatus.DELIVERED
            fulfillmentStatus == "IN_PROGRESS" -> OrderDisplayStatus.IN_TRANSIT
            fulfillmentStatus == "OPEN" || fulfillmentStatus == "UNFULFILLED" -> OrderDisplayStatus.PROCESSING
            else -> OrderDisplayStatus.PENDING
        }
}
