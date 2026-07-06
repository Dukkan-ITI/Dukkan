package com.dukkan.domain.model

sealed interface AgenticSearchResult {
    data class AwaitingClarification(
        val request: ClarificationRequest
    ) : AgenticSearchResult

    data class Success(
        val query: String,
        val products: List<SearchProduct>,
        val totalCount: Int,
        val message: String? = null
    ) : AgenticSearchResult

    data class Error(
        val errorType: GeminiError,
        val message: String? = null
    ) : AgenticSearchResult
}

enum class GeminiError {
    RateLimited,
    Timeout,
    Unknown,
    ClarificationLimitReached,
    MaxStepsReached,
    SearchFailed,
    TimeoutExceeded
}

