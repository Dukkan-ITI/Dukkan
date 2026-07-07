package com.dukkan.ai_agent.registry

import com.dukkan.ai_agent.contract.AiTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRegistry @Inject constructor(
    private val tasks: Set<@JvmSuppressWildcards AiTask<*, *>>
) {
    @Suppress("UNCHECKED_CAST")
    fun <I, O> getTask(taskClass: Class<out AiTask<I, O>>): AiTask<I, O> {
        val task = tasks.find { taskClass.isInstance(it) }
            ?: throw IllegalArgumentException("Task ${taskClass.simpleName} not found in registry")
        return task as AiTask<I, O>
    }
}
