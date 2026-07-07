package com.dukkan.domain.repository

import com.dukkan.domain.model.AgenticSearchResult
import kotlinx.coroutines.flow.Flow

interface AiSearchRepository {
    fun startSearch(query: String): Flow<AgenticSearchResult>

    fun resumeWithAnswer(sessionId: String, answer: String): Flow<AgenticSearchResult>
}
