package com.dukkan.domain.usecase.search

import com.dukkan.domain.model.SearchIntent
import com.dukkan.domain.repository.GeminiRepository
import javax.inject.Inject

class InterpretVoiceSearchUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    suspend operator fun invoke(audioBytes: ByteArray, mimeType: String): SearchIntent {
        return geminiRepository.interpretQuery(audioBytes, mimeType)
    }
}
