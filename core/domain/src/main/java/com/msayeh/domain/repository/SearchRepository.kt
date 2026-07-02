package com.msayeh.domain.repository

import com.msayeh.domain.model.PredictiveSearchResult
import com.msayeh.domain.model.SearchResult

interface SearchRepository {
    suspend fun searchProducts(
        query: String,
        first: Int = 20,
        after: String? = null,
        filters: com.msayeh.domain.model.SearchFilter? = null
    ): Result<SearchResult>

    suspend fun predictiveSearch(query: String): Result<PredictiveSearchResult>
}
