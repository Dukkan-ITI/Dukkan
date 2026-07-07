package com.dukkan.data.ai.handler

import com.dukkan.ai_agent.contract.AiError
import com.dukkan.ai_agent.contract.AiMessage
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiRole
import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.ai_agent.contract.ToolDefinition
import com.dukkan.ai_agent.contract.ToolParameter
import com.dukkan.ai_agent.contract.ToolParameterType
import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.mapper.toAiToolResponse
import com.dukkan.data.ai.mapper.toShopifySearchToolArgs
import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.AiSearchError
import com.dukkan.domain.model.ClarificationRequest
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.repository.SearchRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

data class SearchTaskInput(val sessionId: String? = null, val query: String)

@Singleton
class SearchTask @Inject constructor(
    private val searchRepository: SearchRepository
) : AiTask<SearchTaskInput, AgenticSearchResult> {
    override val taskId: String = "SearchTask"

    private val sessions = mutableMapOf<String, MutableList<AiMessage>>()
    private val lastQuery = mutableMapOf<String, String>()
    private val lastProducts = mutableMapOf<String, List<SearchProduct>>()

    override suspend fun execute(
        input: SearchTaskInput,
        orchestrator: AiAgentOrchestrator
    ): Result<AgenticSearchResult> {
        val sessionId = input.sessionId ?: UUID.randomUUID().toString()
        val history = sessions.getOrPut(sessionId) { mutableListOf() }
        
        history.add(AiMessage(role = AiRole.User, text = input.query))
        lastQuery[sessionId] = input.query

        val tools = listOf(
            ToolDefinition(
                name = SearchAiConstants.SEARCH_SHOPIFY_PRODUCTS,
                description = "Search Dukkan's Shopify storefront for products that match the shopper intent.",
                properties = mapOf(
                    "query" to ToolParameter(ToolParameterType.String, "Short Shopify product search query."),
                    "category" to ToolParameter(ToolParameterType.String, "Optional product category or product type."),
                    "color" to ToolParameter(ToolParameterType.String, "Optional product color."),
                    "size" to ToolParameter(ToolParameterType.String, "Optional product size or variant option."),
                    "minPrice" to ToolParameter(ToolParameterType.Number, "Optional minimum price."),
                    "maxPrice" to ToolParameter(ToolParameterType.Number, "Optional maximum price."),
                    "availableOnly" to ToolParameter(ToolParameterType.Boolean, "Whether to show only products available for sale."),
                    "vendors" to ToolParameter(ToolParameterType.String, "Comma separated list of requested vendors or brands.")
                ),
                required = listOf("query")
            ),
            ToolDefinition(
                name = SearchAiConstants.ASK_CLARIFYING_QUESTION,
                description = "Ask the user a conversational question to clarify what they are looking for.",
                properties = mapOf(
                    "question" to ToolParameter(ToolParameterType.String, "The conversational question to ask the user.")
                ),
                required = listOf("question")
            )
        )

        var turns = 0
        while (turns < 4) {
            val request = AiRequest(
                systemInstruction = SearchAiConstants.SYSTEM_INSTRUCTION.trimIndent(),
                messages = history.toList(),
                tools = tools
            )
            
            val responseResult = orchestrator.generate(request)
            if (responseResult.isFailure) {
                sessions.remove(sessionId)
                lastQuery.remove(sessionId)
                lastProducts.remove(sessionId)
                return Result.success(AgenticSearchResult.Error(AiSearchError.Unknown))
            }
            val response = responseResult.getOrThrow()
            
            val assistantMsg = AiMessage(
                role = AiRole.Model,
                text = response.text.orEmpty(),
                toolCalls = response.toolCalls
            )
            history.add(assistantMsg)

            if (response.toolCalls.isEmpty()) {
                val finalQuery = lastQuery.remove(sessionId) ?: input.query
                val finalProducts = lastProducts.remove(sessionId) ?: emptyList()
                sessions.remove(sessionId)
                return Result.success(AgenticSearchResult.Success(
                    query = finalQuery,
                    products = finalProducts,
                    totalCount = finalProducts.size,
                    message = response.text
                ))
            }

            val toolCall = response.toolCalls.first()
            if (toolCall.name == SearchAiConstants.SEARCH_SHOPIFY_PRODUCTS) {
                if (turns >= 2 && lastProducts.containsKey(sessionId)) {
                    // Prevent infinite loops if model tries to search again instead of answering
                    val finalQuery = lastQuery.remove(sessionId) ?: input.query
                    val finalProducts = lastProducts.remove(sessionId) ?: emptyList()
                    sessions.remove(sessionId)
                    val message = if (finalProducts.isEmpty()) {
                        "Sorry, I couldn't find any products matching your search."
                    } else {
                        "Here are the products I found for you."
                    }
                    return Result.success(AgenticSearchResult.Success(
                        query = finalQuery,
                        products = finalProducts,
                        totalCount = finalProducts.size,
                        message = message
                    ))
                }

                val searchArgs = toolCall.arguments.toShopifySearchToolArgs(input.query)
                val searchResult = searchRepository.searchProducts(
                    query = searchArgs.query,
                    first = 20,
                    after = null,
                    filters = searchArgs.filters
                ).getOrThrow() 

                val searchTerms = listOfNotNull(searchArgs.intent.category, searchArgs.intent.query)
                    .flatMap { it.split("\\s+".toRegex()) }
                    .filter { it.length > 2 }
                    .map { it.lowercase() }

                val relevantProducts = if (searchTerms.isEmpty()) {
                    searchResult.products
                } else {
                    searchResult.products.filter { product ->
                        val productText = "${product.title} ${product.productType} ${product.vendor}".lowercase()
                        searchTerms.any { term -> productText.contains(term) }
                    }
                }
                
                lastQuery[sessionId] = searchArgs.query
                lastProducts[sessionId] = relevantProducts

                history.add(AiMessage(
                    role = AiRole.Tool,
                    toolResultName = toolCall.name,
                    toolResultId = toolCall.id,
                    toolResult = relevantProducts.toAiToolResponse(relevantProducts.size)
                ))
            } else if (toolCall.name == SearchAiConstants.ASK_CLARIFYING_QUESTION) {
                val question = toolCall.arguments["question"]?.jsonPrimitive?.content ?: "Can you please provide more details?"
                return Result.success(AgenticSearchResult.AwaitingClarification(
                    ClarificationRequest(
                        sessionId = sessionId,
                        question = question
                    )
                ))
            } else {
                // Hallucinated or unknown tool call
                sessions.remove(sessionId)
                lastQuery.remove(sessionId)
                lastProducts.remove(sessionId)
                return Result.success(AgenticSearchResult.Error(AiSearchError.Unknown, "AI attempted to call unknown tool: ${toolCall.name}"))
            }
            turns++
        }
        
        sessions.remove(sessionId)
        lastQuery.remove(sessionId)
        lastProducts.remove(sessionId)
        return Result.success(AgenticSearchResult.Error(AiSearchError.MaxStepsReached))
    }
}
