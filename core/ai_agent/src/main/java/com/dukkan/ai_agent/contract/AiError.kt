package com.dukkan.ai_agent.contract

sealed class AiError : Exception() {
    object NetworkError : AiError()
    object ModelUnavailable : AiError()
    object InvalidResponse : AiError()
    object Timeout : AiError()
    object TimeoutExceeded : AiError()
    object ClarificationLimitReached : AiError()
    object MaxStepsReached : AiError()
    object ToolExecutionFailed : AiError()
    object Unknown : AiError()
}
