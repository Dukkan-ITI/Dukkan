package com.dukkan.domain.model

sealed class ProductComparisonResult {
    data class Comparison(
        val features: List<ComparisonFeature>,
        val recommendationText: String?
    ) : ProductComparisonResult()

    data class NeedsMoreInfo(
        val question: McqQuestion,
        val sessionId: String
    ) : ProductComparisonResult()

    data class Error(val message: String) : ProductComparisonResult()
}

data class ComparisonFeature(
    val featureName: String,
    val product1Value: String,
    val product2Value: String,
    val winner: Int? // 1, 2, or null
)

data class McqQuestion(
    val text: String,
    val options: List<String>
)
