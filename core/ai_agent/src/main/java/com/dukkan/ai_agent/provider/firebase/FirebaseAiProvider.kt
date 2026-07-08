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
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                val prompt = request.messages.joinToString(separator = "\n") { message ->
                    when (message.role) {
                        AiRole.System -> "System: ${message.text}"
                        AiRole.User -> "User: ${message.text}"
                        AiRole.Model, AiRole.Assistant -> {
                            val toolCallsStr = message.toolCalls.joinToString { "Called tool ${it.name} with args ${it.args}" }
                            val textStr = message.text
                            val combined = listOfNotNull(textStr.takeIf { it.isNotBlank() }, toolCallsStr.takeIf { it.isNotBlank() }).joinToString(" | ")
                            "Assistant: $combined"
                        }
                        AiRole.Tool -> "Tool ${message.toolResultName.orEmpty()} result: ${message.text} ${
                            message.toolResult?.toString().orEmpty()
                        }"
                    }
                }
                val response = model.generateContent(prompt)
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

