package com.dukkan.domain.usecase.search

import com.dukkan.domain.repository.AiSearchRepository
import javax.inject.Inject

class AgenticSearchUseCase @Inject constructor(
    private val aiSearchRepository: AiSearchRepository
) {
    operator fun invoke(query: String) = aiSearchRepository.startSearch(query)
}
