package com.dukkan.data.mapper

import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery
import com.msayeh.domain.model.Money
import com.msayeh.domain.model.PageInfo
import com.msayeh.domain.model.PredictiveSearchResult
import com.msayeh.domain.model.SearchCollection
import com.msayeh.domain.model.SearchProduct
import com.msayeh.domain.model.SearchProductVariant
import com.msayeh.domain.model.SearchResult

// ─── SearchProducts ───────────────────────────────────────────────────────────

fun SearchProductsQuery.Data.toSearchResult(): SearchResult {
    val connection = search
    val products = connection.edges.mapNotNull { edge ->
        edge.node.onProduct?.toSearchProduct()
    }
    return SearchResult(
        products = products,
        pageInfo = PageInfo(
            hasNextPage = connection.pageInfo.hasNextPage,
            endCursor = connection.pageInfo.endCursor
        ),
        totalCount = connection.totalCount
    )
}

fun SearchProductsQuery.OnProduct.toSearchProduct(): SearchProduct {
    return SearchProduct(
        id = id,
        title = title,
        vendor = vendor,
        availableForSale = availableForSale,
        price = priceRange.minVariantPrice.toDomainMoney(),
        imageUrl = featuredImage?.url?.let { it as? String },
        imageAltText = featuredImage?.altText,
        variants = variants.nodes.map { it.toSearchProductVariant() }
    )
}

fun SearchProductsQuery.Node1.toSearchProductVariant(): SearchProductVariant {
    return SearchProductVariant(
        id = id,
        title = title,
        availableForSale = availableForSale,
        quantityAvailable = quantityAvailable,
        price = price.toDomainMoney()
    )
}

private fun SearchProductsQuery.MinVariantPrice.toDomainMoney(): Money =
    Money(amount = (amount as String).toBigDecimal(), currencyCode = currencyCode.rawValue)

private fun SearchProductsQuery.Price.toDomainMoney(): Money =
    Money(amount = (amount as String).toBigDecimal(), currencyCode = currencyCode.rawValue)

// ─── PredictiveSearch ─────────────────────────────────────────────────────────

fun PredictiveSearchQuery.Data.toPredictiveSearchResult(): PredictiveSearchResult {
    val result = predictiveSearch ?: return PredictiveSearchResult(emptyList(), emptyList())
    return PredictiveSearchResult(
        products = result.products.map { it.toSearchProduct() },
        collections = result.collections.map { it.toSearchCollection() }
    )
}

private fun PredictiveSearchQuery.Product.toSearchProduct(): SearchProduct {
    return SearchProduct(
        id = id,
        title = title,
        vendor = vendor,
        availableForSale = availableForSale,
        price = priceRange.minVariantPrice.toDomainMoney(),
        imageUrl = featuredImage?.url?.let { it as? String },
        imageAltText = featuredImage?.altText,
        variants = emptyList()
    )
}

private fun PredictiveSearchQuery.Collection.toSearchCollection(): SearchCollection {
    return SearchCollection(
        id = id,
        title = title,
        handle = handle,
        imageUrl = image?.url?.let { it as? String }
    )
}

private fun PredictiveSearchQuery.MinVariantPrice.toDomainMoney(): Money =
    Money(amount = (amount as String).toBigDecimal(), currencyCode = currencyCode.rawValue)
