package com.dukkan.ai_agent.provider.firebase

import com.dukkan.ai_agent.contract.AiChatRequest
import com.dukkan.ai_agent.contract.AiChatResponse
import com.dukkan.ai_agent.contract.AiMessage
import com.dukkan.ai_agent.contract.AiProvider
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiResponse
import com.dukkan.ai_agent.contract.AiRole
import com.dukkan.ai_agent.contract.AiToolCall
import com.dukkan.ai_agent.contract.ModelDescriptor
import com.dukkan.ai_agent.contract.ToolDefinition
import com.dukkan.ai_agent.contract.ToolParameterType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiProvider @Inject constructor() : AiProvider {
    override val providerId: String = "gemini"
    override val descriptor: ModelDescriptor = ModelDescriptor(providerId, "gemini-2.5-flash-lite")

    override suspend fun sendChat(request: AiChatRequest): AiChatResponse? {
        val result = generate(AiRequest(messages = request.messages, tools = request.tools))
        if (result.isSuccess) {
            val res = result.getOrThrow()
            return AiChatResponse(
                assistantMessage = res.text?.let { AiMessage(AiRole.Model, it) },
                toolCall = res.toolCalls.firstOrNull(),
                content = res.text
            )
        }
        return null
    }

    override suspend fun generate(request: AiRequest): Result<AiResponse> =
        withContext(Dispatchers.IO) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-2.5-flash-lite",
                    systemInstruction = request.systemInstruction?.let {
                        com.google.firebase.ai.type.content {
                            text(
                                it
                            )
                        }
                    },
                    tools = if (request.tools.isNotEmpty()) listOf(
                        Tool.functionDeclarations(
                            request.tools.map(
                                ::toFunctionDeclaration
                            )
                        )
                    ) else emptyList()
                )
                // Build a structured multi-turn history so Gemini can correlate its
                // function calls with their responses. Flattening this to plain text
                // breaks function calling: the model never "sees" the tool result and
                // keeps re-issuing the same call until the turn budget runs out.
                val contents = request.messages.mapNotNull { message ->
                    when (message.role) {
                        AiRole.System -> null // provided via systemInstruction
                        AiRole.User -> content(role = "user") { text(message.text) }
                        AiRole.Model, AiRole.Assistant -> {
                            if (message.text.isBlank() && message.toolCalls.isEmpty()) {
                                null
                            } else {
                                content(role = "model") {
                                    if (message.text.isNotBlank()) text(message.text)
                                    message.toolCalls.forEach { call ->
                                        part(FunctionCallPart(call.name, call.args))
                                    }
                                }
                            }
                        }
                        AiRole.Tool -> content(role = "function") {
                            val responseJson = when (val result = message.toolResult) {
                                is JsonObject -> result
                                null -> buildJsonObject { put("result", message.text) }
                                else -> buildJsonObject { put("result", result) }
                            }
                            part(FunctionResponsePart(message.toolResultName.orEmpty(), responseJson))
                        }
                    }
                }
                val response = model.generateContent(contents)
                val toolCall = response.functionCalls.firstOrNull()?.let {
                    AiToolCall(id = it.name, name = it.name, args = it.args, arguments = it.args)
                }
                Result.success(
                    AiResponse(
                        text = response.text,
                        toolCalls = listOfNotNull(toolCall)
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun toFunctionDeclaration(tool: ToolDefinition): FunctionDeclaration =
        FunctionDeclaration(
            name = tool.name,
            description = tool.description,
            parameters = tool.properties.mapValues { (_, parameter) ->
                when (parameter.type) {
                    ToolParameterType.String -> Schema.string(parameter.description)
                    ToolParameterType.Number -> Schema.double(parameter.description)
                    ToolParameterType.Boolean -> Schema.boolean(parameter.description)
                }
            },
            optionalParameters = (tool.properties.keys - tool.required.toSet()).toList()
        )
}

