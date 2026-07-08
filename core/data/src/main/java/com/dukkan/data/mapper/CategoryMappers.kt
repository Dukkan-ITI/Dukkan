package com.dukkan.data.mapper

import com.dukkan.GetCollectionsQuery
import com.dukkan.data.source.local.entity.HomeCategoryEntity
import com.dukkan.domain.model.Category.Category

fun HomeCategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        handle = handle
    )
}

fun Category.toHomeEntity(): HomeCategoryEntity {
    return HomeCategoryEntity(
        id = id,
        name = name,
        handle = handle
    )
}

fun GetCollectionsQuery.Node.toDomainCategory(): Category {
    return Category(
        id = this.id,
        name = this.title,
        handle = this.handle,
        urlImage = this.image?.url?.toString()
    )
}
