package com.dukkan.ai_agent.router

import com.dukkan.ai_agent.contract.AiError
import com.dukkan.ai_agent.contract.AiProvider
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiResponse
import com.dukkan.ai_agent.contract.TaskType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AiProvider>
) {
    private val providerFailures = mutableMapOf<String, Long>()

    fun providersFor(taskType: TaskType): List<AiProvider> {
        return providers.toList()
    }

    suspend fun generateWithFallback(request: AiRequest): Result<AiResponse> {
        val now = System.currentTimeMillis()
        val availableProviders = providers.filter {
            val lastFailure = providerFailures[it.providerId] ?: 0L
            now - lastFailure > 60_000L // 1 minute cooldown
        }

        val providersToTry = availableProviders.ifEmpty { providers } // If all failed, try all again

        for (provider in providersToTry) {
            val result = provider.generate(request)
            if (result.isSuccess) {
                providerFailures.remove(provider.providerId)
                return result
            } else {
                providerFailures[provider.providerId] = System.currentTimeMillis()
            }
        }

        return Result.failure(AiError.ModelUnavailable)
    }
}
