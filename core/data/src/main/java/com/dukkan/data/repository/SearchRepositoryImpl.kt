package com.dukkan.data.repository

import com.dukkan.data.mapper.toPredictiveSearchResult
import com.dukkan.data.mapper.toSearchResult
import com.dukkan.data.source.remote.apollo.SearchDataSource
import com.msayeh.domain.model.PredictiveSearchResult
import com.msayeh.domain.model.SearchResult
import com.msayeh.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val searchDataSource: SearchDataSource
) : SearchRepository {

    override suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?,
        filters: com.msayeh.domain.model.SearchFilter?
    ): Result<SearchResult> = runCatching {
        searchDataSource.searchProducts(query, first, after, filters)
            ?.toSearchResult()
            ?: SearchResult(emptyList(), com.msayeh.domain.model.PageInfo(false, null), 0)
    }

    override suspend fun predictiveSearch(query: String): Result<PredictiveSearchResult> =
        runCatching {
            searchDataSource.predictiveSearch(query)
                ?.toPredictiveSearchResult()
                ?: PredictiveSearchResult(emptyList(), emptyList())
        }
}
