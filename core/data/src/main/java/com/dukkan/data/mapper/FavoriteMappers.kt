package com.dukkan.data.mapper

import com.dukkan.data.source.local.entity.FavoriteEntity
import com.msayeh.domain.model.FavoriteProduct

fun FavoriteEntity.toDomainModel() = FavoriteProduct(
    id = id,
    title = title,
    imageUrl = imageUrl,
    price = price,
    currencyCode = currencyCode
)

fun FavoriteProduct.toEntity() = FavoriteEntity(
    id = id,
    title = title,
    imageUrl = imageUrl,
    price = price,
    currencyCode = currencyCode
)