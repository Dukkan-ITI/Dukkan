package com.dukkan.data.source

import com.dukkan.data.BuildConfig
import com.dukkan.data.util.AiSearchConstants.ASK_CLARIFYING_QUESTION
import com.dukkan.data.util.AiSearchConstants.DEFAULT_OLLAMA_MODEL
import com.dukkan.data.util.AiSearchConstants.SEARCH_SHOPIFY_PRODUCTS
import com.dukkan.data.util.AiSearchConstants.SYSTEM_INSTRUCTION
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
class OllamaRemoteSource @Inject constructor(
    private val client: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    fun startMessages(): MutableList<JsonObject> =
        mutableListOf(message(role = "system", content = SYSTEM_INSTRUCTION.trimIndent()))

    suspend fun sendChat(messages: List<JsonObject>): OllamaChatResponse = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.OLLAMA_BASE_URL.trimEnd('/')
        val apiKey = BuildConfig.OLLAMA_API_KEY
        if (baseUrl.contains("ollama.com") && apiKey.isBlank()) {
            error("OLLAMA_API_KEY_MISSING")
        }

        val body = JsonObject(
            mapOf(
                "model" to JsonPrimitive(BuildConfig.OLLAMA_MODEL.ifBlank { DEFAULT_OLLAMA_MODEL }),
                "messages" to JsonArray(messages),
                "tools" to JsonArray(listOf(searchShopifyProductsTool, askClarifyingQuestionTool)),
                "stream" to JsonPrimitive(false),
                "options" to JsonObject(
                    mapOf(
                        "temperature" to JsonPrimitive(0)
                    )
                )
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
            error("Ollama API ${response.code}: $responseBody")
        }

        parseResponse(responseBody)
    }

    fun userMessage(content: String): JsonObject = message(role = "user", content = content)

    fun assistantMessage(content: String): JsonObject = message(role = "assistant", content = content)

    fun toolMessage(functionName: String, response: JsonObject): JsonObject =
        JsonObject(
            mapOf(
                "role" to JsonPrimitive("tool"),
                "tool_name" to JsonPrimitive(functionName),
                "content" to JsonPrimitive(response.toString())
            )
        )

    private fun parseResponse(responseBody: String): OllamaChatResponse {
        val root = json.parseToJsonElement(responseBody).jsonObject
        val assistantMessage = root["message"]?.jsonObject ?: JsonObject(emptyMap())
        val toolCall = assistantMessage["tool_calls"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.let(::parseToolCall)

        return OllamaChatResponse(
            content = assistantMessage["content"]?.jsonPrimitive?.contentOrNull,
            toolCall = toolCall,
            assistantMessage = assistantMessage
        )
    }

    private fun parseToolCall(toolCall: JsonObject): OllamaToolCall? {
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

        return OllamaToolCall(
            name = name,
            args = arguments.toMap()
        )
    }

    private fun message(role: String, content: String): JsonObject =
        JsonObject(
            mapOf(
                "role" to JsonPrimitive(role),
                "content" to JsonPrimitive(content)
            )
        )

    private val searchShopifyProductsTool: JsonObject =
        functionTool(
            name = SEARCH_SHOPIFY_PRODUCTS,
            description = "Search Dukkan's Shopify storefront for products that match the shopper intent.",
            properties = mapOf(
                "query" to stringSchema("Short Shopify product search query."),
                "category" to stringSchema("Optional product category or product type."),
                "color" to stringSchema("Optional product color."),
                "size" to stringSchema("Optional product size or variant option."),
                "maxPrice" to numberSchema("Optional maximum price."),
                "availableOnly" to booleanSchema("Whether to show only products available for sale.")
            ),
            required = listOf("query")
        )

    private val askClarifyingQuestionTool: JsonObject =
        functionTool(
            name = ASK_CLARIFYING_QUESTION,
            description = "Ask one short clarification when the shopper's request is too ambiguous to search well.",
            properties = mapOf(
                "question" to stringSchema("A concise clarification question for the shopper.")
            ),
            required = listOf("question")
        )

    private fun functionTool(
        name: String,
        description: String,
        properties: Map<String, JsonObject>,
        required: List<String>
    ): JsonObject =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("function"),
                "function" to JsonObject(
                    mapOf(
                        "name" to JsonPrimitive(name),
                        "description" to JsonPrimitive(description),
                        "parameters" to JsonObject(
                            mapOf(
                                "type" to JsonPrimitive("object"),
                                "properties" to JsonObject(properties),
                                "required" to JsonArray(required.map(::JsonPrimitive))
                            )
                        )
                    )
                )
            )
        )

    private fun stringSchema(description: String): JsonObject =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("string"),
                "description" to JsonPrimitive(description)
            )
        )

    private fun numberSchema(description: String): JsonObject =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("number"),
                "description" to JsonPrimitive(description)
            )
        )

    private fun booleanSchema(description: String): JsonObject =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("boolean"),
                "description" to JsonPrimitive(description)
            )
        )
}

data class OllamaChatResponse(
    val content: String?,
    val toolCall: OllamaToolCall?,
    val assistantMessage: JsonObject
)

data class OllamaToolCall(
    val name: String,
    val args: Map<String, JsonElement>
)
