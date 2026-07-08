package com.dukkan.data.repository

import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.handler.ProductComparisonTask
import com.dukkan.data.ai.handler.ProductComparisonTaskInput
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductComparisonResult
import com.dukkan.domain.repository.ProductComparisonRepository
import javax.inject.Inject

class ProductComparisonRepositoryImpl @Inject constructor(
    private val aiOrchestrator: AiAgentOrchestrator
) : ProductComparisonRepository {
    override suspend fun compareProducts(
        product1: Product,
        product2: Product,
        sessionId: String?,
        answer: String?
    ): Result<ProductComparisonResult> {
        val input = ProductComparisonTaskInput(
            product1 = product1,
            product2 = product2,
            sessionId = sessionId,
            answer = answer
        )
        return aiOrchestrator.run(ProductComparisonTask::class.java, input)
    }
}
