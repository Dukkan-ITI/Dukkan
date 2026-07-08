package com.dukkan.data.ai.mapper

import com.dukkan.domain.model.SearchFilter
import com.dukkan.domain.model.SearchIntent
import com.dukkan.domain.model.SearchProduct
import kotlinx.serialization.json.JsonArray
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
    val minPrice = doubleValue("minPrice")
    val maxPrice = doubleValue("maxPrice")
    val availableOnly = booleanValue("availableOnly") ?: false
    val enrichedQuery = listOf(query, category, color, size)
        .filterNot { it.isNullOrBlank() }
        .joinToString(separator = " ")

    return ShopifySearchToolArgs(
        query = enrichedQuery,
        filters = SearchFilter(
            minPrice = minPrice,
            maxPrice = maxPrice,
            availableOnly = availableOnly,
            productTypes = category?.takeIf { it.isNotBlank() }?.let(::listOf) ?: emptyList()
        ),
        intent = SearchIntent(
            query = query,
            category = category,
            color = color,
            size = size,
            minPrice = minPrice,
            maxPrice = maxPrice,
            availableOnly = availableOnly
        )
    )
}

fun List<SearchProduct>.toAiToolResponse(totalCount: Int): JsonObject =
    JsonObject(
        mapOf(
            "totalCount" to JsonPrimitive(totalCount),
            "products" to JsonArray(
                take(8).map { product ->
                    JsonObject(
                        mapOf(
                            "id" to JsonPrimitive(product.id),
                            "title" to JsonPrimitive(product.title),
                            "vendor" to JsonPrimitive(product.vendor),
                            "productType" to JsonPrimitive(product.productType),
                            "priceAmount" to JsonPrimitive(product.price.amount),
                            "currencyCode" to JsonPrimitive(product.price.currencyCode),
                            "availableForSale" to JsonPrimitive(product.availableForSale),
                            "variantId" to JsonPrimitive(product.variants.firstOrNull()?.id ?: "")
                        )
                    )
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
