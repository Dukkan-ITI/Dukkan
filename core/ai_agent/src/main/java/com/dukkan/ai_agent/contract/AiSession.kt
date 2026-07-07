package com.dukkan.ai_agent.contract

data class AiSession(
    val id: String,
    val taskType: TaskType,
    val messages: MutableList<AiMessage> = mutableListOf(),
    var lastInput: String = "",
    var clarifications: Int = 0,
    var toolCalls: Int = 0
)
