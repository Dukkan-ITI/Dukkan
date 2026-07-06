package com.dukkan.domain.repository

import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.SearchIntent
import kotlinx.coroutines.flow.Flow

interface GeminiRepository {
    fun startSearch(query: String): Flow<AgenticSearchResult>

    fun resumeWithAnswer(sessionId: String, answer: String): Flow<AgenticSearchResult>
}
