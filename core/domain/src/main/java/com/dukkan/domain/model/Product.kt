package com.dukkan.domain.model

data class Product(
    val id: String,
    val title: String,
    val featuredImage: NetworkImage?,
    val minPrice: Money,
    val maxPrice: Money,
    val description: String?,
    val productType: String?,
    val images: List<NetworkImage>?,
    val variants: List<ProductVariant>?,
    val reviews: List<Review> = emptyList(),
    val averageRating: Float? = null,
)
