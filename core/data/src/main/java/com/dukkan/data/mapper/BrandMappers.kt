package com.dukkan.data.mapper

import com.dukkan.GetBrandsQuery
import com.dukkan.domain.model.Brand
import kotlin.collections.mapNotNull


fun List<GetBrandsQuery.Node>.toDomainBrands(): List<Brand> {
    return this
        .mapNotNull { node -> node.vendor.takeIf { it.isNotBlank() } }
        .distinct()
        .map { vendor -> Brand(id = vendor, name = vendor) }
}