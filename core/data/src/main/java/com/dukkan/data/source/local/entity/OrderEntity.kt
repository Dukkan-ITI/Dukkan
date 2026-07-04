package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.model.orders.OrderLineItem
import com.dukkan.domain.model.orders.ShippingAddress
import java.math.BigDecimal

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: Int,
    val processedAt: String,
    val financialStatus: String?,
    val fulfillmentStatus: String?,
    val totalPriceAmount: String,
    val totalPriceCurrency: String,
    val subtotalPriceAmount: String,
    val subtotalPriceCurrency: String,
    val lineItemsJson: String,
    val shippingAddressJson: String?
) {
    fun toDomainModel(): Order {
        val lineItems = try {
            val jsonArray = org.json.JSONArray(lineItemsJson)
            val items = mutableListOf<OrderLineItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    OrderLineItem(
                        title = obj.getString("title"),
                        quantity = obj.getInt("quantity"),
                        totalPrice = Money(
                            amount = BigDecimal(obj.getString("originalTotalPriceAmount")),
                            currencyCode = obj.getString("originalTotalPriceCurrency")
                        ),
                        variantId = obj.getString("variantId"),
                        variantTitle = obj.getString("variantTitle"),
                        imageUrl = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) obj.getString("imageUrl") else null
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }

        val shippingAddress = try {
            if (shippingAddressJson.isNullOrEmpty()) null
            else {
                val obj = org.json.JSONObject(shippingAddressJson)
                ShippingAddress(
                    firstName = obj.optString("firstName", ""),
                    lastName = obj.optString("lastName", ""),
                    address1 = obj.optString("address1", ""),
                    city = obj.optString("city", ""),
                    country = obj.optString("country", ""),
                    phone = obj.optString("phone", "")
                )
            }
        } catch (e: Exception) {
            null
        }

        return Order(
            id = id,
            orderNumber = orderNumber,
            processedAt = processedAt,
            financialStatus = financialStatus,
            fulfillmentStatus = fulfillmentStatus,
            totalPrice = Money(BigDecimal(totalPriceAmount), totalPriceCurrency),
            subtotalPrice = Money(BigDecimal(subtotalPriceAmount), subtotalPriceCurrency),
            lineItems = lineItems,
            shippingAddress = shippingAddress
        )
    }

    companion object {
        fun fromDomainModel(order: Order): OrderEntity {
            val lineItemsJson = org.json.JSONArray().apply {
                order.lineItems.forEach { item ->
                    val obj = org.json.JSONObject()
                    obj.put("title", item.title)
                    obj.put("quantity", item.quantity)
                    obj.put("originalTotalPriceAmount", item.totalPrice.amount.toPlainString())
                    obj.put("originalTotalPriceCurrency", item.totalPrice.currencyCode)
                    obj.put("variantId", item.variantId)
                    obj.put("variantTitle", item.variantTitle)
                    item.imageUrl?.let { obj.put("imageUrl", it) }
                    put(obj)
                }
            }.toString()

            val shippingAddressJson = order.shippingAddress?.let { address ->
                org.json.JSONObject().apply {
                    put("firstName", address.firstName)
                    put("lastName", address.lastName)
                    put("address1", address.address1)
                    put("city", address.city)
                    put("country", address.country)
                    put("phone", address.phone)
                }.toString()
            }

            return OrderEntity(
                id = order.id,
                orderNumber = order.orderNumber,
                processedAt = order.processedAt,
                financialStatus = order.financialStatus,
                fulfillmentStatus = order.fulfillmentStatus,
                totalPriceAmount = order.totalPrice.amount.toPlainString(),
                totalPriceCurrency = order.totalPrice.currencyCode,
                subtotalPriceAmount = order.subtotalPrice.amount.toPlainString(),
                subtotalPriceCurrency = order.subtotalPrice.currencyCode,
                lineItemsJson = lineItemsJson,
                shippingAddressJson = shippingAddressJson
            )
        }
    }
}
