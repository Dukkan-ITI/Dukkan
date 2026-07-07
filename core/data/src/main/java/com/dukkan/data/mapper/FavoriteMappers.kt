package com.dukkan.data.mapper

import com.dukkan.data.source.local.entity.FavoriteEntity
import com.dukkan.domain.model.FavoriteProduct

fun FavoriteEntity.toDomainModel() = FavoriteProduct(
    id = id,
    title = title,
    imageUrl = imageUrl,
    price = price,
    currencyCode = currencyCode,
    rating = rating,
    reviewCount = reviewCount
)

fun FavoriteProduct.toEntity() = FavoriteEntity(
    id = id,
    title = title,
    imageUrl = imageUrl,
    price = price,
    currencyCode = currencyCode,
    rating = rating,
    reviewCount = reviewCount
)