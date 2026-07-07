package com.dukkan.ai_agent.provider.ollama

import com.dukkan.ai_agent.BuildConfig
import com.dukkan.ai_agent.contract.AiError
import com.dukkan.ai_agent.contract.AiMessage
import com.dukkan.ai_agent.contract.AiProvider
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiResponse
import com.dukkan.ai_agent.contract.AiRole
import com.dukkan.ai_agent.contract.AiToolCall
import com.dukkan.ai_agent.contract.AiChatRequest
import com.dukkan.ai_agent.contract.AiChatResponse
import com.dukkan.ai_agent.contract.ModelDescriptor
import com.dukkan.ai_agent.contract.ToolDefinition
import com.dukkan.ai_agent.contract.ToolParameterType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class OllamaProvider @Inject constructor(
    private val client: OkHttpClient
) : AiProvider {
    override val providerId: String = "ollama"
    override val descriptor: ModelDescriptor = ModelDescriptor(providerId, BuildConfig.OLLAMA_MODEL)

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

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    override suspend fun generate(request: AiRequest): Result<AiResponse> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = BuildConfig.OLLAMA_BASE_URL.trimEnd('/')
            val apiKey = BuildConfig.OLLAMA_API_KEY
            if (baseUrl.contains("ollama.com") && apiKey.isBlank()) {
                return@withContext Result.failure(AiError.ModelUnavailable)
            }

            val body = JsonObject(
                mapOf(
                    "model" to JsonPrimitive(BuildConfig.OLLAMA_MODEL),
                    "messages" to JsonArray(request.messages.map(::messageToJson)),
                    "tools" to JsonArray(request.tools.map(::toolToJson)),
                    "stream" to JsonPrimitive(false)
                )
            )

            val requestBuilder = Request.Builder()
                .url("$baseUrl/chat")
                .post(body.toString().toRequestBody(contentType))
                .header("Content-Type", "application/json")

            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(AiError.InvalidResponse)
            }

            Result.success(parseResponse(responseBody))
        } catch (e: Exception) {
            Result.failure(AiError.NetworkError)
        }
    }

    private fun parseResponse(responseBody: String): AiResponse {
        val root = json.parseToJsonElement(responseBody).jsonObject
        val assistantMessage = root["message"]?.jsonObject ?: JsonObject(emptyMap())
        val toolCall = assistantMessage["tool_calls"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.let(::parseToolCall)

        val content = assistantMessage["content"]?.jsonPrimitive?.contentOrNull
        return AiResponse(
            text = content,
            toolCalls = listOfNotNull(toolCall)
        )
    }

    private fun parseToolCall(toolCall: JsonObject): AiToolCall? {
        val function = toolCall["function"]?.jsonObject ?: return null
        val name = function["name"]?.jsonPrimitive?.contentOrNull ?: return null
        val arguments = when (val args = function["arguments"]) {
            is JsonObject -> args
            is JsonPrimitive -> args.contentOrNull
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() }
                ?: JsonObject(emptyMap())
            else -> JsonObject(emptyMap())
        }
        return AiToolCall(id = name, name = name, args = arguments.toMap(), arguments = arguments.toMap())
    }

    private fun messageToJson(message: AiMessage): JsonObject {
        val role = when (message.role) {
            AiRole.System -> "system"
            AiRole.User -> "user"
            AiRole.Model, AiRole.Assistant -> "assistant"
            AiRole.Tool -> "tool"
        }
        val values = mutableMapOf<String, JsonElement>(
            "role" to JsonPrimitive(role),
            "content" to JsonPrimitive(message.text)
        )
        if (message.role == AiRole.Tool && message.toolResultName != null) {
            values["tool_name"] = JsonPrimitive(message.toolResultName)
        }
        return JsonObject(values)
    }

    private fun toolToJson(tool: ToolDefinition): JsonObject =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("function"),
                "function" to JsonObject(
                    mapOf(
                        "name" to JsonPrimitive(tool.name),
                        "description" to JsonPrimitive(tool.description),
                        "parameters" to JsonObject(
                            mapOf(
                                "type" to JsonPrimitive("object"),
                                "properties" to JsonObject(tool.properties.mapValues { (_, param) ->
                                    JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive(param.type.toOllamaType()),
                                            "description" to JsonPrimitive(param.description)
                                        )
                                    )
                                }),
                                "required" to JsonArray(tool.required.map(::JsonPrimitive))
                            )
                        )
                    )
                )
            )
        )

    private fun ToolParameterType.toOllamaType(): String = when (this) {
        ToolParameterType.String -> "string"
        ToolParameterType.Number -> "number"
        ToolParameterType.Boolean -> "boolean"
    }
}
