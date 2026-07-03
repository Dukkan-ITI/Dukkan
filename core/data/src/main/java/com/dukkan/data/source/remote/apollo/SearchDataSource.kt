package com.dukkan.data.source.remote.apollo

import com.dukkan.PredictiveSearchQuery
import com.dukkan.SearchProductsQuery

interface SearchDataSource {
    suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?,
        filters: com.dukkan.domain.model.SearchFilter? = null
    ): SearchProductsQuery.Data?

    suspend fun predictiveSearch(query: String): PredictiveSearchQuery.Data?
}
