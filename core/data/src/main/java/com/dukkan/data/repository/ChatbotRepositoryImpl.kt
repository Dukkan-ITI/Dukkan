package com.dukkan.data.repository

import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.handler.ChatbotTask
import com.dukkan.data.ai.handler.ChatbotTaskInput
import com.dukkan.domain.model.chatbot.ChatMessage
import com.dukkan.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val orchestrator: AiAgentOrchestrator
) : ChatbotRepository {

    override suspend fun sendMessage(query: String, sessionId: String): Result<ChatMessage> {
        if (query.isBlank()) return Result.failure(Exception("Query is blank"))

        return orchestrator.run(
            ChatbotTask::class.java,
            ChatbotTaskInput(sessionId = sessionId, message = query)
        ).map { result ->
            ChatMessage(
                text = result.reply,
                isFromUser = false,
                products = result.products,
                orders = result.orders
            )
        }
    }
}