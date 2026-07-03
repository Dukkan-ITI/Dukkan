package com.dukkan.domain.usecase.search

import com.dukkan.domain.model.PredictiveSearchResult
import com.dukkan.domain.repository.SearchRepository
import javax.inject.Inject

class PredictiveSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(query: String): Result<PredictiveSearchResult> =
        searchRepository.predictiveSearch(query)
}
