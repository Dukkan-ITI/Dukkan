package com.dukkan.domain.usecase.search

import com.dukkan.domain.repository.AiSearchRepository
import javax.inject.Inject

class ResumeSearchClarificationUseCase @Inject constructor(
    private val aiSearchRepository: AiSearchRepository
) {
    operator fun invoke(sessionId: String, answer: String) =
        aiSearchRepository.resumeWithAnswer(sessionId, answer)
}
