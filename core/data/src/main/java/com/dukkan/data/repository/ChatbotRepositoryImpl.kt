package com.dukkan.data.repository

import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.handler.ChatbotTask
import com.dukkan.data.ai.handler.ChatbotTaskInput
import com.dukkan.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val orchestrator: AiAgentOrchestrator
) : ChatbotRepository {

    override suspend fun sendMessage(query: String, sessionId: String): Result<String> {
        if (query.isBlank()) return Result.success("")

        return orchestrator.run(
            ChatbotTask::class.java,
            ChatbotTaskInput(sessionId = sessionId, message = query)
        ).map { it.reply }
    }
}