package com.dukkan.domain.usecase.search

import com.dukkan.domain.repository.GeminiRepository
import javax.inject.Inject

class AgenticSearchUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    operator fun invoke(query: String) = geminiRepository.startSearch(query)
}
