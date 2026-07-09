package com.dukkan.data.mapper

import com.dukkan.CollectionProductsQuery
import com.dukkan.GetProductsByVendorQuery
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.data.source.local.entity.HomeProductEntity
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.NetworkImage
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductSummary
import com.dukkan.domain.model.ProductVariant
import com.dukkan.domain.model.Review
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal

private val gson = Gson()

fun HomeProductEntity.toDomainModel(): Product {
    val dummyReviews = List(reviewCount ?: 0) {
        Review(id = "", authorName = "", rating = rating?.toInt() ?: 0, title = "", body = "", createdAt = "", approved = true)
    }

    val images: List<NetworkImage>? = imagesJson?.let {
        val type = object : TypeToken<List<NetworkImage>>() {}.type
        gson.fromJson(it, type)
    }

    val variants: List<ProductVariant>? = variantsJson?.let {
        val type = object : TypeToken<List<ProductVariant>>() {}.type
        gson.fromJson(it, type)
    }

    return Product(
        id = id,
        title = title,
        vendor = vendor,
        featuredImage = imageUrl?.let { NetworkImage(url = it, blurredUrl = null, altText = null) },
        minPrice = Money(amount = BigDecimal(priceAmount), currencyCode = currencyCode),
        maxPrice = Money(amount = BigDecimal(priceAmount), currencyCode = currencyCode),
        description = description,
        productType = productType,
        images = images,
        variants = variants,
        reviews = dummyReviews,
        averageRating = rating,
    )
}

fun Product.toHomeEntity(): HomeProductEntity {
    return HomeProductEntity(
        id = id,
        title = title,
        vendor = vendor,
        imageUrl = featuredImage?.url,
        priceAmount = minPrice.amount.toPlainString(),
        currencyCode = minPrice.currencyCode,
        rating = averageRating,
        reviewCount = reviews.size,
        productType = productType,
        description = description,
        imagesJson = gson.toJson(images),
        variantsJson = gson.toJson(variants)
    )
}

fun ProductQuery.Product.toDomainModel(): Product {
    val reviews = metafields
        .filterNotNull()
        .flatMap { it.references?.edges.orEmpty() }
        .mapNotNull { edge ->
            val obj = edge?.node?.onMetaobject ?: return@mapNotNull null
            val isApproved = obj.approved?.value == "true"
            if (!isApproved) return@mapNotNull null
            Review(
                id         = obj.id,
                authorName = obj.customerName?.value.orEmpty(),
                rating     = obj.rating?.value?.toIntOrNull() ?: 0,
                title      = obj.reviewTitle?.value.orEmpty(),
                body       = obj.body?.value.orEmpty(),
                createdAt  = obj.createdAt?.value.orEmpty(),
                approved   = true,
            )
        }
    val averageRating = if (reviews.isEmpty()) null
                        else reviews.map { it.rating }.average().toFloat()

    return Product(
        id            = id,
        title         = title,
        vendor        = vendor,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice      = priceRange.minVariantPrice.toDomainModel(),
        maxPrice      = priceRange.maxVariantPrice.toDomainModel(),
        description   = description,
        productType   = productType,
        images        = images.toDomainModel(),
        variants      = variants.toDomainModel(),
        reviews       = reviews,
        averageRating = averageRating,
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
        quantityAvailable = quantityAvailable ?: 0,
        compareAtPrice = null,
        selectedOptions = emptyList(),
        product = ProductSummary(id = "", title = "", vendor = "")
    )
}

fun ProductQuery.FeaturedImage.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)
fun ProductQuery.Node.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)
fun ProductQuery.Image.toDomainModel(): NetworkImage = networkImage(url, thumbhash, altText)

fun ProductQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun ProductQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun ProductQuery.Price.toDomainModel(): Money = money(amount, currencyCode.rawValue)

fun ProductsQuery.Node.toDomainModel(): Product {
    val ratings = metafields?.flatMap { it?.references?.edges.orEmpty() }
        ?.mapNotNull { it?.node?.onMetaobject?.rating?.value?.toIntOrNull() }
        ?: emptyList()

    val averageRating = if (ratings.isEmpty()) null else ratings.average().toFloat()
    val dummyReviews = ratings.map { Review(id = "", authorName = "", rating = it, title = "", body = "", createdAt = "", approved = true) }

    return Product(
        id = id,
        title = title,
        vendor = vendor,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = description,
        productType = null,
        images = null,
        variants = null,
        reviews = dummyReviews,
        averageRating = averageRating,
    )
}

fun ProductsQuery.FeaturedImage.toDomainModel(): NetworkImage? {
    val urlStr = url as? String ?: return null
    return NetworkImage(url = urlStr, blurredUrl = thumbhash, altText = null)
}

fun ProductsQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun ProductsQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)

fun CollectionProductsQuery.Node.toDomainModel(): Product {
    val ratings = metafields?.flatMap { it?.references?.edges.orEmpty() }
        ?.mapNotNull { it?.node?.onMetaobject?.rating?.value?.toIntOrNull() }
        ?: emptyList()

    val averageRating = if (ratings.isEmpty()) null else ratings.average().toFloat()
    val dummyReviews = ratings.map { Review(id = "", authorName = "", rating = it, title = "", body = "", createdAt = "", approved = true) }

    return Product(
        id = id,
        title = title,
        vendor = vendor,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = description,
        productType = null,
        images = null,
        variants = null,
        reviews = dummyReviews,
        averageRating = averageRating,
    )
}

fun CollectionProductsQuery.FeaturedImage.toDomainModel(): NetworkImage? {
    val urlStr = url as? String ?: return null
    return NetworkImage(url = urlStr, blurredUrl = thumbhash, altText = null)
}

fun CollectionProductsQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun CollectionProductsQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)

private fun networkImage(url: Any?, thumbhash: String?, altText: String?): NetworkImage {
    val urlStr = url as? String ?: throw IllegalArgumentException("Image URL is required")
    return NetworkImage(url = urlStr, blurredUrl = thumbhash, altText = altText)
}

private fun money(amount: Any?, currencyCode: String): Money {
    val parsed = (amount as? String)?.toBigDecimal()
        ?: throw IllegalArgumentException("Amount is required")
    return Money(amount = parsed, currencyCode = currencyCode)
}

fun GetProductsByVendorQuery.Node.toDomainModel(): Product {
    val ratings = metafields?.flatMap { it?.references?.edges.orEmpty() }
        ?.mapNotNull { it?.node?.onMetaobject?.rating?.value?.toIntOrNull() }
        ?: emptyList()

    val averageRating = if (ratings.isEmpty()) null else ratings.average().toFloat()
    val dummyReviews = ratings.map { Review(id = "", authorName = "", rating = it, title = "", body = "", createdAt = "", approved = true) }

    return Product(
        id = id,
        title = title,
        vendor = vendor,
        featuredImage = featuredImage?.toDomainModel(),
        minPrice = priceRange.minVariantPrice.toDomainModel(),
        maxPrice = priceRange.maxVariantPrice.toDomainModel(),
        description = null,
        productType = null,
        images = null,
        variants = null,
        reviews = dummyReviews,
        averageRating = averageRating,
    )
}

fun GetProductsByVendorQuery.FeaturedImage.toDomainModel(): NetworkImage? {
    val urlStr = url as? String ?: return null
    return NetworkImage(url = urlStr, blurredUrl = thumbhash, altText = null)
}

fun GetProductsByVendorQuery.MinVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
fun GetProductsByVendorQuery.MaxVariantPrice.toDomainModel(): Money = money(amount, currencyCode.rawValue)
