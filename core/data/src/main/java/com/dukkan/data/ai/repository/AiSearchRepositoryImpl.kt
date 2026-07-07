package com.dukkan.data.ai.repository

import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.handler.SearchTask
import com.dukkan.data.ai.handler.SearchTaskInput
import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.AiSearchError
import com.dukkan.domain.repository.AiSearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class AiSearchRepositoryImpl @Inject constructor(
    private val orchestrator: AiAgentOrchestrator
) : AiSearchRepository {


    override fun startSearch(query: String): Flow<AgenticSearchResult> = flow {
        if (query.isBlank()) return@flow
        val result = orchestrator.run(SearchTask::class.java, SearchTaskInput(query = query))
        emit(result.getOrElse { AgenticSearchResult.Error(AiSearchError.Unknown) })
    }

    override fun resumeWithAnswer(sessionId: String, answer: String): Flow<AgenticSearchResult> = flow {
        val result = orchestrator.run(SearchTask::class.java, SearchTaskInput(sessionId = sessionId, query = answer))
        emit(result.getOrElse { AgenticSearchResult.Error(AiSearchError.Unknown) })
    }
}
