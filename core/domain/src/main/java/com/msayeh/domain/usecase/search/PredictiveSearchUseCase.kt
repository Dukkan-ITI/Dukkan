package com.msayeh.domain.usecase.search

import com.msayeh.domain.model.PredictiveSearchResult
import com.msayeh.domain.repository.SearchRepository
import javax.inject.Inject

class PredictiveSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(query: String): Result<PredictiveSearchResult> =
        searchRepository.predictiveSearch(query)
}
