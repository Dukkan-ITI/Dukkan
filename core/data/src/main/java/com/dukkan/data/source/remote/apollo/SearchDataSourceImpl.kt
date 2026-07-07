package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery
import com.dukkan.type.ProductFilter
import com.dukkan.type.PriceRangeFilter
import com.dukkan.domain.model.SearchFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : SearchDataSource {

    private fun SearchFilter.toApolloFilters(): List<ProductFilter> {
        val filters = mutableListOf<ProductFilter>()

        if (availableOnly) {
            filters += ProductFilter(available = Optional.present(true))
        }
        if (minPrice != null || maxPrice != null) {
            filters += ProductFilter(
                price = Optional.present(PriceRangeFilter(
                    min = Optional.presentIfNotNull(minPrice),
                    max = Optional.presentIfNotNull(maxPrice)
                ))
            )
        }
        // Instead of ProductFilter for vendors/types, we will append them to the query string to support OR logic
        return filters
    }

    override suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?,
        filters: SearchFilter?
    ): SearchProductsQuery.Data? {
        val apolloFilters = filters?.toApolloFilters()
        
        var finalQuery = query
        if (filters != null) {
            if (filters.vendors.isNotEmpty()) {
                val vendorQuery = filters.vendors.joinToString(" OR ") { "vendor:\"$it\"" }
                finalQuery = if (finalQuery.isBlank()) vendorQuery else "$finalQuery AND ($vendorQuery)"
            }
            if (filters.productTypes.isNotEmpty()) {
                val typeQuery = filters.productTypes.joinToString(" OR ") { "product_type:\"$it\"" }
                finalQuery = if (finalQuery.isBlank()) typeQuery else "$finalQuery AND ($typeQuery)"
            }
        }

        val response = apolloClient.query(
            SearchProductsQuery(
                query = Optional.present(finalQuery),
                first = Optional.present(first),
                after = Optional.presentIfNotNull(after),
                filters = Optional.presentIfNotNull(apolloFilters?.takeIf { it.isNotEmpty() })
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
