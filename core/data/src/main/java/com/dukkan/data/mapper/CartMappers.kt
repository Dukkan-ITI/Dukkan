package com.dukkan.data.mapper

import com.dukkan.data.source.local.entity.CartEntity
import com.dukkan.domain.model.CartItem
import com.dukkan.domain.model.Money

fun CartEntity.toDomainModel(): CartItem? {
    val amount = price.toBigDecimalOrNull() ?: return null
    return CartItem(
        id = id,
        title = title,
        imageUrl = imageUrl,
        price = Money(
            amount = amount,
            currencyCode = currencyCode,
        ),
        size = size,
        quantity = quantity
    )
}

fun CartItem.toEntity(): CartEntity {
    return CartEntity(
        id = id,
        title = title,
        imageUrl = imageUrl,
        price = price.amount.toString(),
        currencyCode = price.currencyCode,
        size = size,
        quantity = quantity
    )
}