package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery
import com.dukkan.type.ProductFilter
import com.dukkan.type.PriceRangeFilter
import com.msayeh.domain.model.SearchFilter
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
        vendors.forEach { vendor ->
            filters += ProductFilter(productVendor = Optional.present(vendor))
        }
        productTypes.forEach { type ->
            filters += ProductFilter(productType = Optional.present(type))
        }

        return filters
    }

    override suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?,
        filters: SearchFilter?
    ): SearchProductsQuery.Data? {
        val apolloFilters = filters?.toApolloFilters()
        val response = apolloClient.query(
            SearchProductsQuery(
                query = Optional.present(query),
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
