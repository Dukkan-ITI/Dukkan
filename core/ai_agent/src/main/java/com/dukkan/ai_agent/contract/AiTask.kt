package com.dukkan.ai_agent.contract

import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator

interface AiTask<TInput, TOutput> {
    val taskId: String
    suspend fun execute(input: TInput, orchestrator: AiAgentOrchestrator): Result<TOutput>
}
