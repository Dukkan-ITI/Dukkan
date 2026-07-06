package com.dukkan.data.mapper

import com.dukkan.domain.model.ClarificationRequest
import com.dukkan.domain.model.SearchFilter
import com.dukkan.domain.model.SearchIntent
import com.dukkan.domain.model.SearchProduct
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

data class ShopifySearchToolArgs(
    val query: String,
    val filters: SearchFilter,
    val intent: SearchIntent
)

fun Map<String, JsonElement>.toShopifySearchToolArgs(fallbackQuery: String): ShopifySearchToolArgs {
    val query = stringValue("query")?.takeIf { it.isNotBlank() } ?: fallbackQuery
    val category = stringValue("category")
    val color = stringValue("color")
    val size = stringValue("size")
    val maxPrice = doubleValue("maxPrice")
    val availableOnly = booleanValue("availableOnly") ?: false
    val enrichedQuery = listOf(query, color, size)
        .filterNot { it.isNullOrBlank() }
        .joinToString(separator = " ")

    return ShopifySearchToolArgs(
        query = enrichedQuery,
        filters = SearchFilter(
            maxPrice = maxPrice,
            availableOnly = availableOnly,
            productTypes = category?.takeIf { it.isNotBlank() }?.let(::listOf) ?: emptyList()
        ),
        intent = SearchIntent(
            query = query,
            category = category,
            color = color,
            size = size,
            maxPrice = maxPrice,
            availableOnly = availableOnly
        )
    )
}

fun Map<String, JsonElement>.toClarificationRequest(sessionId: String): ClarificationRequest =
    ClarificationRequest(
        question = stringValue("question")?.takeIf { it.isNotBlank() }
            ?: "What kind of product are you looking for?",
        sessionId = sessionId
    )

fun List<SearchProduct>.toGeminiToolResponse(totalCount: Int): JsonObject =
    JsonObject(
        mapOf(
            "totalCount" to JsonPrimitive(totalCount),
            "products" to JsonPrimitive(
                take(8).joinToString(separator = "\n") { product ->
                    "${product.title} | ${product.vendor} | ${product.productType} | ${product.price.amount} ${product.price.currencyCode} | available=${product.availableForSale}"
                }
            )
        )
    )

private fun Map<String, JsonElement>.stringValue(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun Map<String, JsonElement>.doubleValue(key: String): Double? =
    this[key]?.jsonPrimitive?.doubleOrNull

private fun Map<String, JsonElement>.booleanValue(key: String): Boolean? =
    this[key]?.jsonPrimitive?.booleanOrNull
