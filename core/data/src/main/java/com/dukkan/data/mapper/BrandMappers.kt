package com.dukkan.data.mapper

import com.dukkan.GetCollectionsQuery
import com.dukkan.domain.model.Brand
import kotlin.collections.map

fun List<GetCollectionsQuery.Node>.toDomainBrands(): List<Brand> {
    return this.map { node ->
        Brand(
            id = node.id,
            name = node.title,
            urlImage = node.image?.url?.toString()
        )
    }
}
