package com.dukkan.data.mapper

import com.dukkan.data.source.local.entity.CartEntity
import com.dukkan.domain.model.CartItem

 fun CartEntity.toDomainModel(): CartItem {
    return CartItem(
        id = id,
        title = title,
        imageUrl = imageUrl,
        price = price,
        currencyCode = currencyCode,
        size = size,
        quantity = quantity
    )
}

 fun CartItem.toEntity(): CartEntity {
    return CartEntity(
        id = id,
        title = title,
        imageUrl = imageUrl,
        price = price,
        currencyCode = currencyCode,
        size = size,
        quantity = quantity
    )
}