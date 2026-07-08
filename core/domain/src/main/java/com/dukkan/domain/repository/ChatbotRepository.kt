package com.dukkan.domain.repository

import com.dukkan.domain.model.chatbot.ChatMessage

interface ChatbotRepository {
    suspend fun sendMessage(query: String, sessionId: String = "default"): Result<ChatMessage>
}