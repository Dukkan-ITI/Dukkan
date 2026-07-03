package com.dukkan.data.mapper

import com.dukkan.data.source.local.entity.FavoriteEntity
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Money

fun FavoriteEntity.toDomainModel(): FavoriteProduct? {
    val amount = price.toBigDecimalOrNull() ?: return null
    return FavoriteProduct(
        id = id,
        title = title,
        imageUrl = imageUrl,
        price = Money(
            amount = amount,
            currencyCode = currencyCode
        )
    )
}

fun FavoriteProduct.toEntity() = FavoriteEntity(
    id = id,
    title = title,
    imageUrl = imageUrl,
    price = price.amount.toString(),
    currencyCode = price.currencyCode
)