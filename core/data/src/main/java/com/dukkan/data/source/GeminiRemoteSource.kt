package com.dukkan.data.source

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton
import com.dukkan.data.util.GeminiConstants.ASK_CLARIFYING_QUESTION
import com.dukkan.data.util.GeminiConstants.MODEL_NAME
import com.dukkan.data.util.GeminiConstants.SEARCH_SHOPIFY_PRODUCTS
import com.dukkan.data.util.GeminiConstants.SYSTEM_INSTRUCTION
@Singleton
class GeminiRemoteSource @Inject constructor() {

    private val searchShopifyProductsTool = FunctionDeclaration(
        name = SEARCH_SHOPIFY_PRODUCTS,
        description = "Search Dukkan's Shopify storefront for products that match the shopper intent.",
        parameters = mapOf(
            "query" to Schema.string("Short Shopify product search query."),
            "category" to Schema.string("Optional product category or product type."),
            "color" to Schema.string("Optional product color."),
            "size" to Schema.string("Optional product size or variant option."),
            "maxPrice" to Schema.double("Optional maximum price."),
            "availableOnly" to Schema.boolean("Whether to show only products available for sale.")
        ),
        optionalParameters = listOf("category", "color", "size", "maxPrice", "availableOnly")
    )

    private val askClarifyingQuestionTool = FunctionDeclaration(
        name = ASK_CLARIFYING_QUESTION,
        description = "Ask one short clarification when the shopper's request is too ambiguous to search well.",
        parameters = mapOf(
            "question" to Schema.string("A concise clarification question for the shopper.")
        )
    )

    private val model: GenerativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = MODEL_NAME,
            tools = listOf(
                Tool.functionDeclarations(
                    listOf(searchShopifyProductsTool, askClarifyingQuestionTool)
                )
            ),
            systemInstruction = content {
                text(SYSTEM_INSTRUCTION)
            }
        )

    fun startChat(): Chat = model.startChat()

    suspend fun sendUserMessage(chat: Chat, message: String): GenerateContentResponse =
        chat.sendMessage(message)

    suspend fun sendFunctionResponse(
        chat: Chat,
        functionName: String,
        response: JsonObject
    ): GenerateContentResponse =
        chat.sendMessage(
            content("function") {
                part(FunctionResponsePart(functionName, response))
            }
        )
}
