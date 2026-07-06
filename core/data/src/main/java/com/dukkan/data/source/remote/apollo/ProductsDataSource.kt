package com.dukkan.data.source.remote.apollo

import com.dukkan.GetCollectionsQuery
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery

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

    suspend fun fetchCategories(): List<GetCollectionsQuery.Node>

    suspend fun getProductsByCollectionHandle(
        handle: String,
        first: Int,
        after: String? = null,
        country: String,
        language: String,
    ): List<com.dukkan.domain.model.Product>

    suspend fun fetchBrands(): List<GetCollectionsQuery.Node>

    suspend fun getProductsByVendor(
        vendor: String,
        first: Int,
        after: String? = null,
        country: String,
        language: String,
    ): List<com.dukkan.domain.model.Product>
}