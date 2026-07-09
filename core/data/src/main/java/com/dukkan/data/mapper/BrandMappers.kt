package com.dukkan.data.mapper

import com.dukkan.GetCollectionsQuery
import com.dukkan.data.source.local.entity.HomeBrandEntity
import com.dukkan.domain.model.Brand
import kotlin.collections.map

fun HomeBrandEntity.toDomainModel(): Brand {
    return Brand(
        id = id,
        name = name
    )
}

fun Brand.toHomeEntity(): HomeBrandEntity {
    return HomeBrandEntity(
        id = id,
        name = name
    )
}

fun List<GetCollectionsQuery.Node>.toDomainBrands(): List<Brand> {
    return this.map { node ->
        Brand(
            id = node.id,
            name = node.title,
            urlImage = node.image?.url?.toString()
        )
    }
}
