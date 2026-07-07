package com.dukkan.ai_agent.contract

interface AiProvider {
    val providerId: String
    val descriptor: ModelDescriptor
    suspend fun generate(request: AiRequest): Result<AiResponse>
    suspend fun sendChat(request: AiChatRequest): AiChatResponse?
}
