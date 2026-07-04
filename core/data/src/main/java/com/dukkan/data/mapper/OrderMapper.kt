package com.dukkan.data.mapper

import com.dukkan.GetCustomerOrdersQuery
import com.msayeh.domain.model.Money
import com.msayeh.domain.model.orders.Order
import com.msayeh.domain.model.orders.OrderLineItem
import com.msayeh.domain.model.orders.ShippingAddress
import java.math.BigDecimal

object OrderMapper {

    fun map(node: GetCustomerOrdersQuery.Node): Order {
        return Order(
            id = node.id,
            orderNumber = node.orderNumber,
            processedAt = node.processedAt as? String ?: "",
            financialStatus = node.financialStatus?.name,
            fulfillmentStatus = node.fulfillmentStatus?.name,
            totalPrice = Money(
                amount = (node.currentTotalPrice.amount as? String)?.let { BigDecimal(it) } ?: BigDecimal.ZERO,
                currencyCode = node.currentTotalPrice.currencyCode.name
            ),
            subtotalPrice = Money(
                amount = (node.currentSubtotalPrice.amount as? String)?.let { BigDecimal(it) } ?: BigDecimal.ZERO,
                currencyCode = node.currentSubtotalPrice.currencyCode.name
            ),
            lineItems = node.lineItems.edges.map { edge ->
                OrderLineItem(
                    title = edge.node.title,
                    quantity = edge.node.quantity,
                    totalPrice = Money(
                        amount = (edge.node.originalTotalPrice.amount as? String)?.let { BigDecimal(it) } ?: BigDecimal.ZERO,
                        currencyCode = edge.node.originalTotalPrice.currencyCode.name
                    ),
                    variantId = edge.node.variant?.id ?: "",
                    variantTitle = edge.node.variant?.title ?: "",
                    imageUrl = edge.node.variant?.image?.url as? String
                )
            },
            shippingAddress = node.shippingAddress?.let { address ->
                ShippingAddress(
                    firstName = address.firstName,
                    lastName = address.lastName,
                    address1 = address.address1,
                    city = address.city,
                    country = address.country,
                    phone = address.phone
                )
            }
        )
    }
}
