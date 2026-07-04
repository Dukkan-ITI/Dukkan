package com.dukkan.settings.mapper

import com.example.design_system.components.OrderStatus
import com.example.design_system.components.OrderUi
import com.msayeh.domain.model.asString
import com.msayeh.domain.model.orders.Order
import com.msayeh.domain.model.orders.OrderDisplayStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun Order.toOrderUi(): OrderUi = OrderUi(
    id = "#JN-$orderNumber",
    date = formatOrderDate(processedAt),
    itemCount = lineItems.size,
    total = totalPrice.asString(),
    status = displayStatus.toOrderStatus(),
)

private fun OrderDisplayStatus.toOrderStatus(): OrderStatus = when (this) {
    OrderDisplayStatus.DELIVERED -> OrderStatus.DELIVERED
    OrderDisplayStatus.IN_TRANSIT,
    OrderDisplayStatus.PROCESSING,
    OrderDisplayStatus.PENDING,
    OrderDisplayStatus.CANCELLED -> OrderStatus.IN_TRANSIT
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
