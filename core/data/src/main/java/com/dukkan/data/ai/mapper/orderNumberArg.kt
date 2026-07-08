package com.dukkan.data.ai.mapper

import com.dukkan.domain.model.orders.Order
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement

fun Map<String, JsonElement>.orderNumberArg(): Int? =
    this["orderNumber"]?.jsonPrimitive?.contentOrNull
        ?.filter { it.isDigit() }
        ?.toIntOrNull()

fun List<Order>.toAiToolResponse(requestedOrderNumber: Int?): JsonObject = JsonObject(
    mapOf(
        "requestedOrderNumber" to (requestedOrderNumber?.let { JsonPrimitive(it) } ?: JsonNull),
        "found" to JsonPrimitive(isNotEmpty()),
        "orders" to JsonArray(
            take(5).map { order ->
                JsonObject(
                    mapOf(
                        "orderNumber" to JsonPrimitive(order.orderNumber),
                        "status" to JsonPrimitive(order.displayStatus.name),
                        "processedAt" to JsonPrimitive(order.processedAt),
                        "total" to JsonPrimitive(order.totalPrice.asString()),
                        "itemCount" to JsonPrimitive(order.lineItems.sumOf { it.quantity })
                    )
                )
            }
        )
    )
)

fun errorToolResult(message: String): JsonObject =
    JsonObject(mapOf("error" to JsonPrimitive(message)))

fun successToolResult(message: String): JsonObject =
    JsonObject(mapOf("success" to JsonPrimitive(message)))

fun unknownToolResult(name: String): JsonObject =
    JsonObject(mapOf("error" to JsonPrimitive("Unknown tool requested: $name")))