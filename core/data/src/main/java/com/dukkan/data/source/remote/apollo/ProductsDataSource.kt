package com.dukkan.data.source.remote.apollo

import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.VariantPricesQuery

interface ProductsDataSource {
    suspend fun getProducts(
        first: Int = 10,
        after: String? = null,
        country: String,
        language: String,
    ): ProductsQuery.Data?

    suspend fun getProductById(
        id: String,
        country: String,
        language: String,
    ): ProductQuery.Product?

    suspend fun getVariantPrices(
        variantIds: List<String>,
        country: String,
    ): VariantPricesQuery.Data?
}
