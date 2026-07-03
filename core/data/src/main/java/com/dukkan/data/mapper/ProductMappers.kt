package com.dukkan.data.mapper

import com.dukkan.CollectionProductsQuery
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.NetworkImage
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductVariant

fun ProductQuery.Product.toDomainModel(): Product {
    return Product(
        id = id,
        title = title,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = description,
        productType = productType,
        images = images.toDomainModel(),
        variants = variants.toDomainModel(),
    )
}

fun ProductQuery.Images.toDomainModel(): List<NetworkImage> = nodes.map { it.toDomainModel() }

fun ProductQuery.Variants.toDomainModel(): List<ProductVariant> = nodes.map { it.toDomainModel() }

fun ProductQuery.Node1.toDomainModel(): ProductVariant {
    return ProductVariant(
        id = id,
        title = title,
        price = price.toDomainModel(),
        image = image?.toDomainModel(),
        availableForSale = availableForSale,
        quantityAvailable = quantityAvailable ?: 0
    )
}

fun ProductQuery.FeaturedImage.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)
fun ProductQuery.Node.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)
fun ProductQuery.Image.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)

fun ProductQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun ProductQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun ProductQuery.Price.toDomainModel(): Money = money(amount, currencyCode.rawValue)

fun ProductsQuery.Node.toDomainModel(): Product {
    return Product(
        id = id,
        title = title,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = description,
        productType = null,
        images = null,
        variants = null,
    )
}

fun ProductsQuery.FeaturedImage.toDomainModel(): NetworkImage? {
    if (url !is String) return null
    return NetworkImage(url = url, blurredUrl = thumbhash, altText = null)
}

fun ProductsQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)

fun ProductsQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)

private fun networkImage(url: Any?, thumbhash: String?, altText: String?): NetworkImage {
    require(url is String) { "Image URL is required" }
    return NetworkImage(url = url, blurredUrl = thumbhash, altText = altText)
}

private fun money(amount: Any?, currencyCode: String): Money {
    val parsed = (amount as? String)?.toBigDecimal()
        ?: throw IllegalArgumentException("Amount is required")
    return Money(amount = parsed, currencyCode = currencyCode)
}

fun CollectionProductsQuery.Node.toDomainModel(): Product {
    return Product(
        id = id,
        title = title,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = description,
        productType = null,
        images = null,
        variants = null,
    )
}

fun CollectionProductsQuery.FeaturedImage.toDomainModel(): NetworkImage? {
    if (url !is String) return null
    return NetworkImage(url = url, blurredUrl = thumbhash, altText = null)
}

fun CollectionProductsQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun CollectionProductsQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
