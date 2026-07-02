package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.type.CountryCode
import com.dukkan.type.LanguageCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsDataSourceImpl @Inject constructor(private val apolloClient: ApolloClient) : ProductsDataSource {
    override suspend fun getProducts(
        first: Int,
        after: String?,
        country: String,
        language: String,
    ): ProductsQuery.Data? {
        val response = apolloClient.query(
            ProductsQuery(
                first = Optional.present(first),
                after = Optional.presentIfNotNull(after),
                country = CountryCode.safeValueOf(country),
                language = LanguageCode.safeValueOf(language),
            )
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data
    }

    override suspend fun getProductById(
        id: String,
        country: String,
        language: String,
    ): ProductQuery.Product? {
        val response = apolloClient.query(
            ProductQuery(
                id = id,
                country = CountryCode.safeValueOf(country),
                language = LanguageCode.safeValueOf(language),
            )
        ).execute()

        return response.data?.product
    }
}
