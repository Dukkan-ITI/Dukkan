package com.dukkan.domain.usecase.search

import com.dukkan.domain.repository.GeminiRepository
import javax.inject.Inject

class ResumeSearchClarificationUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    operator fun invoke(sessionId: String, answer: String) =
        geminiRepository.resumeWithAnswer(sessionId, answer)
}
