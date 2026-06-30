package com.dukkan.data.source.remote.apollo

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsDataSourceImpl @Inject constructor(private val apolloClient: ApolloClient) :
    ProductsDataSource {
    override suspend fun getProducts(): ProductsQuery.Products? {
        return apolloClient.query(ProductsQuery()).execute().data?.products
    }

    override suspend fun getProductById(id: String): ProductQuery.Product? {
        val response = apolloClient.query(ProductQuery(id = id)).execute()

        if (response.hasErrors()) {
            Log.e(
                "ProductsRepositoryImpl",
                "getProductById: Error fetching product by ID: $id, Errors: ${response.errors}"
            )
        }

        return response.data?.product
    }
}