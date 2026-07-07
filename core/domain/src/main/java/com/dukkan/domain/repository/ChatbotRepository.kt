package com.dukkan.domain.repository

interface ChatbotRepository {
    suspend fun sendMessage(query: String): Result<String>
}