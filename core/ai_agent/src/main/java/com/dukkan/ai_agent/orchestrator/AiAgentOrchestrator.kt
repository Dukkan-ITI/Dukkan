package com.dukkan.ai_agent.orchestrator

import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiResponse
import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.ai_agent.registry.TaskRegistry
import com.dukkan.ai_agent.router.ModelRouter
import javax.inject.Inject

class AiAgentOrchestrator @Inject constructor(
    private val router: ModelRouter,
    private val registry: TaskRegistry
) {
    suspend fun <I, O> run(taskClass: Class<out AiTask<I, O>>, input: I): Result<O> {
        val task = registry.getTask(taskClass)
        return task.execute(input, this)
    }

    suspend fun generate(request: AiRequest): Result<AiResponse> = router.generateWithFallback(request)
}
