package com.dukkan.ai_agent.contract

import kotlinx.serialization.json.JsonElement

enum class AiRole { User, Model, System, Tool, Assistant }

data class AiMessage(
    val role: AiRole,
    val text: String = "",
    val content: String = text,
    val toolCalls: List<AiToolCall> = emptyList(),
    val toolResultId: String? = null,
    val toolResultName: String? = null,
    val toolName: String? = toolResultName,
    val toolResult: JsonElement? = null
)

data class AiToolCall(
    val id: String,
    val name: String,
    val args: Map<String, JsonElement>,
    val arguments: Map<String, JsonElement> = args
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val properties: Map<String, ToolParameter>,
    val required: List<String> = emptyList()
)

data class ToolParameter(
    val type: ToolParameterType,
    val description: String
)

enum class ToolParameterType { String, Number, Boolean }

data class AiRequest(
    val systemInstruction: String? = null,
    val messages: List<AiMessage>,
    val tools: List<ToolDefinition> = emptyList()
)

data class AiResponse(
    val text: String? = null,
    val toolCalls: List<AiToolCall> = emptyList()
)

data class AiChatRequest(
    val taskType: TaskType,
    val messages: List<AiMessage>,
    val tools: List<ToolDefinition>,
    val maxOutputTokens: Int
)

data class AiChatResponse(
    val assistantMessage: AiMessage?,
    val toolCall: AiToolCall?,
    val content: String?
)

data class ModelDescriptor(
    val providerId: String,
    val modelName: String
)
