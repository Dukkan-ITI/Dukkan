package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : SearchDataSource {

    override suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?
    ): SearchProductsQuery.Data? {
        val response = apolloClient.query(
            SearchProductsQuery(
                query = Optional.present(query),
                first = Optional.present(first),
                after = Optional.presentIfNotNull(after)
            )
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL error")
        }
        return response.data
    }

    override suspend fun predictiveSearch(query: String): PredictiveSearchQuery.Data? {
        val response = apolloClient.query(
            PredictiveSearchQuery(query = Optional.present(query))
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL error")
        }
        return response.data
    }
}
