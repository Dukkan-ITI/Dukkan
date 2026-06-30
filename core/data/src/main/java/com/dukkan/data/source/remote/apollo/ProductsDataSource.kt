package com.dukkan.data.source.remote.apollo

import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery

interface ProductsDataSource {
    suspend fun getProducts(first: Int = 10, after: String? = null): ProductsQuery.Data?
    suspend fun getProductById(id: String): ProductQuery.Product?
}