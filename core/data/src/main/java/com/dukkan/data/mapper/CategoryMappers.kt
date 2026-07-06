package com.dukkan.data.mapper

import com.dukkan.GetCollectionsQuery
import com.dukkan.domain.model.Category.Category

fun GetCollectionsQuery.Node.toDomainCategory(): Category {
    return Category(
        id = this.id,
        name = this.title,
        handle = this.handle
    )
}