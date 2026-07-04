package com.dukkan.domain.repository

import com.dukkan.domain.model.PredictiveSearchResult
import com.dukkan.domain.model.SearchResult

interface SearchRepository {
    suspend fun searchProducts(
        query: String,
        first: Int = 20,
        after: String? = null,
        filters: com.dukkan.domain.model.SearchFilter? = null
    ): Result<SearchResult>

    suspend fun predictiveSearch(query: String): Result<PredictiveSearchResult>
}
