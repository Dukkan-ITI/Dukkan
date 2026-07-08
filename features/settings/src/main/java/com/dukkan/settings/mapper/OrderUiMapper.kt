package com.dukkan.settings.mapper

import com.dukkan.design_system.components.OrderItemUi
import com.dukkan.design_system.components.OrderStatus
import com.dukkan.design_system.components.OrderUi

import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.model.orders.OrderDisplayStatus
import com.dukkan.domain.model.orders.OrderLineItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun Order.toOrderUi(): OrderUi = OrderUi(
    id = "#JN-$orderNumber",
    date = formatOrderDate(processedAt),
    itemCount = lineItems.size,
    total = totalPrice.asString(),
    status = displayStatus.toOrderStatus(),
    isPaid = financialStatus == "PAID",
    items = lineItems.map { it.toCardItemsUi() }
)

fun OrderLineItem.toCardItemsUi() = OrderItemUi(
    title = title,
    quantity = quantity,
    totalPrice = totalPrice.asString(),
    imageUrl = imageUrl
)

private fun OrderDisplayStatus.toOrderStatus(): OrderStatus = when (this) {
    OrderDisplayStatus.DELIVERED -> OrderStatus.DELIVERED
    OrderDisplayStatus.IN_TRANSIT -> OrderStatus.IN_TRANSIT
    OrderDisplayStatus.PROCESSING -> OrderStatus.PROCESSING
    OrderDisplayStatus.PENDING -> OrderStatus.PENDING
    OrderDisplayStatus.CANCELLED -> OrderStatus.CANCELLED
}

private fun formatOrderDate(processedAt: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = parser.parse(processedAt)
        if (date != null) formatter.format(date) else processedAt
    } catch (_: Exception) {
        processedAt
    }
}
