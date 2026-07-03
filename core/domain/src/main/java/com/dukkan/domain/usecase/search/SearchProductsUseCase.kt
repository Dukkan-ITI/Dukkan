package com.dukkan.domain.usecase.search

import com.dukkan.domain.repository.SearchRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        first: Int = 20,
        after: String? = null,
        filters: com.dukkan.domain.model.SearchFilter? = null
    ) = searchRepository.searchProducts(query, first, after, filters)
}
