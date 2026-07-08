package com.dukkan.domain.repository

interface ChatbotRepository {
    suspend fun sendMessage(query: String, sessionId: String = "default"): Result<String>
}