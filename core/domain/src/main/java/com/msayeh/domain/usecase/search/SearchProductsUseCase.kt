package com.msayeh.domain.usecase.search

import com.msayeh.domain.model.SearchResult
import com.msayeh.domain.repository.SearchRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        first: Int = 20,
        after: String? = null,
        filters: com.msayeh.domain.model.SearchFilter? = null
    ) = searchRepository.searchProducts(query, first, after, filters)
}
