package com.dukkan.data.mapper

import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.PageInfo
import com.dukkan.domain.model.PredictiveSearchResult
import com.dukkan.domain.model.SearchCollection
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.SearchProductVariant
import com.dukkan.domain.model.SearchResult

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
        productType = productType,
        availableForSale = availableForSale,
        price = priceRange.minVariantPrice.toDomainMoney(),
        imageUrl = featuredImage?.url?.toString(),
        imageAltText = featuredImage?.altText,
        variants = variants.nodes.map { it.toSearchProductVariant() },
        averageRating = metafields?.mapNotNull { it?.references?.edges?.mapNotNull { edge -> 
            val metaobject = edge.node.onMetaobject
            val ratingField = metaobject?.rating?.value as? String
            ratingField?.toIntOrNull()
        } }?.flatten()?.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
        reviewCount = metafields?.mapNotNull { it?.references?.edges?.mapNotNull { edge -> 
            val metaobject = edge.node.onMetaobject
            val ratingField = metaobject?.rating?.value as? String
            ratingField?.toIntOrNull()
        } }?.flatten()?.size
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
        productType = "",
        availableForSale = availableForSale,
        price = priceRange.minVariantPrice.toDomainMoney(),
        imageUrl = featuredImage?.url?.toString(),
        imageAltText = featuredImage?.altText,
        variants = emptyList(),
        averageRating = metafields?.mapNotNull { it?.references?.edges?.mapNotNull { edge -> 
            val metaobject = edge.node.onMetaobject
            val ratingField = metaobject?.rating?.value as? String
            ratingField?.toIntOrNull()
        } }?.flatten()?.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
        reviewCount = metafields?.mapNotNull { it?.references?.edges?.mapNotNull { edge -> 
            val metaobject = edge.node.onMetaobject
            val ratingField = metaobject?.rating?.value as? String
            ratingField?.toIntOrNull()
        } }?.flatten()?.size
    )
}

private fun PredictiveSearchQuery.Collection.toSearchCollection(): SearchCollection {
    return SearchCollection(
        id = id,
        title = title,
        handle = handle,
        imageUrl = image?.url?.toString()
    )
}

private fun PredictiveSearchQuery.MinVariantPrice.toDomainMoney(): Money =
    Money(amount = (amount as String).toBigDecimal(), currencyCode = currencyCode.rawValue)
