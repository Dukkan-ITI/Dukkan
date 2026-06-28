package com.dukkan.data.source.remote.apollo

import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery

interface ProductsDataSource {
    suspend fun getProducts(): ProductsQuery.Products?
    suspend fun getProductById(id: String): ProductQuery.Product?
}