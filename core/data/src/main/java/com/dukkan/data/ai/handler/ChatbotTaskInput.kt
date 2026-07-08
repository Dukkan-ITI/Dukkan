package com.dukkan.data.ai.handler

import com.dukkan.ai_agent.contract.AiMessage
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiRole
import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.ai_agent.contract.AiToolCall
import com.dukkan.ai_agent.contract.ToolDefinition
import com.dukkan.ai_agent.contract.ToolParameter
import com.dukkan.ai_agent.contract.ToolParameterType
import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.data.ai.mapper.errorToolResult
import com.dukkan.data.ai.mapper.orderNumberArg
import com.dukkan.data.ai.mapper.toAiToolResponse
import com.dukkan.data.ai.mapper.toShopifySearchToolArgs
import com.dukkan.data.ai.mapper.unknownToolResult
import com.dukkan.domain.repository.SearchRepository
import com.dukkan.domain.usecase.order.GetAllOrdersUseCase
import com.dukkan.domain.usecase.order.GetRecentOrdersUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonElement

data class ChatbotTaskInput(
    val sessionId: String,
    val message: String
)

data class ChatbotTaskResult(
    val reply: String
)

/**
 * Drives the assistant chat: keeps a per-session message history, exposes a product-search tool
 * and an order-status tool to the model, executes whichever tool the model calls against Dukkan's
 * own repositories, and loops until the model produces a final conversational reply.
 */
@Singleton
class ChatbotTask @Inject constructor(
    private val searchRepository: SearchRepository,
    private val getRecentOrdersUseCase: GetRecentOrdersUseCase,
    private val getAllOrdersUseCase: GetAllOrdersUseCase
) : AiTask<ChatbotTaskInput, ChatbotTaskResult> {

    override val taskId: String = "ChatbotTask"

    // Conversation history lives here rather than in the ViewModel, so it survives even if the
    // orchestrator/task is reused across screen recompositions, and stays out of the UI layer.
    private val sessions = mutableMapOf<String, MutableList<AiMessage>>()

    private val tools = listOf(
        ToolDefinition(
            name = SearchAiConstants.SEARCH_SHOPIFY_PRODUCTS,
            description = "Search Dukkan's Shopify storefront for products that match the shopper's request.",
            properties = mapOf(
                "query" to ToolParameter(ToolParameterType.String, "Short Shopify product search query."),
                "category" to ToolParameter(ToolParameterType.String, "Optional product category or product type."),
                "color" to ToolParameter(ToolParameterType.String, "Optional product color."),
                "size" to ToolParameter(ToolParameterType.String, "Optional product size or variant option."),
                "minPrice" to ToolParameter(ToolParameterType.Number, "Optional minimum price."),
                "maxPrice" to ToolParameter(ToolParameterType.Number, "Optional maximum price."),
                "availableOnly" to ToolParameter(ToolParameterType.Boolean, "Whether to show only products available for sale.")
            ),
            required = listOf("query")
        ),
        ToolDefinition(
            name = ChatbotAiConstants.GET_ORDER_STATUS,
            description = "Look up the status of the shopper's orders (delivery status, total, item count).",
            properties = mapOf(
                "orderNumber" to ToolParameter(
                    ToolParameterType.String,
                    "The specific order number the shopper mentioned, if any. Leave blank to fetch their most recent orders."
                )
            ),
            required = emptyList()
        )
    )

    override suspend fun execute(
        input: ChatbotTaskInput,
        orchestrator: AiAgentOrchestrator
    ): Result<ChatbotTaskResult> {
        if (input.message.isBlank()) {
            return Result.success(ChatbotTaskResult(reply = ""))
        }

        val history = sessions.getOrPut(input.sessionId) { mutableListOf() }
        history.add(AiMessage(role = AiRole.User, text = input.message))

        var turns = 0
        while (turns < ChatbotAiConstants.MAX_TOOL_TURNS) {
            val request = AiRequest(
                systemInstruction = ChatbotAiConstants.SYSTEM_INSTRUCTION,
                messages = history.toList(),
                tools = tools
            )

            val responseResult = orchestrator.generate(request)
            if (responseResult.isFailure) {
                // Don't poison the session with a broken turn: drop the user's last message so a
                // retry doesn't replay the same failing exchange.
                history.removeLastOrNull()
                return Result.success(ChatbotTaskResult(reply = ChatbotAiConstants.FALLBACK_REPLY))
            }

            val response = responseResult.getOrThrow()
            history.add(
                AiMessage(role = AiRole.Model, text = response.text.orEmpty(), toolCalls = response.toolCalls)
            )

            val toolCall = response.toolCalls.firstOrNull()
            if (toolCall == null) {
                trimHistory(history)
                val reply = response.text?.takeIf { it.isNotBlank() } ?: ChatbotAiConstants.FALLBACK_REPLY
                return Result.success(ChatbotTaskResult(reply = reply))
            }

            val toolResult = executeTool(toolCall)
            history.add(
                AiMessage(
                    role = AiRole.Tool,
                    toolResultName = toolCall.name,
                    toolResultId = toolCall.id,
                    toolResult = toolResult
                )
            )
            turns++
        }

        trimHistory(history)
        return Result.success(ChatbotTaskResult(reply = ChatbotAiConstants.MAX_STEPS_REPLY))
    }

    private suspend fun executeTool(toolCall: AiToolCall): JsonElement = when (toolCall.name) {
        SearchAiConstants.SEARCH_SHOPIFY_PRODUCTS -> executeProductSearch(toolCall)
        ChatbotAiConstants.GET_ORDER_STATUS -> executeOrderLookup(toolCall)
        else -> unknownToolResult(toolCall.name)
    }

    private suspend fun executeProductSearch(toolCall: AiToolCall): JsonElement = try {
        val args = toolCall.arguments.toShopifySearchToolArgs(fallbackQuery = "")
        if (args.query.isBlank()) {
            errorToolResult("No search query was provided.")
        } else {
            val result = searchRepository.searchProducts(
                query = args.query,
                first = 8,
                filters = args.filters
            ).getOrThrow()
            result.products.toAiToolResponse(result.totalCount)
        }
    } catch (e: Exception) {
        errorToolResult("Unable to search products right now.")
    }

    private suspend fun executeOrderLookup(toolCall: AiToolCall): JsonElement = try {
        val requestedNumber = toolCall.arguments.orderNumberArg()
        val orders = if (requestedNumber != null) {
            getAllOrdersUseCase().getOrThrow().orders
        } else {
            getRecentOrdersUseCase().getOrThrow()
        }
        val matches = if (requestedNumber != null) {
            orders.filter { it.orderNumber == requestedNumber }
        } else {
            orders
        }
        matches.toAiToolResponse(requestedNumber)
    } catch (e: Exception) {
        errorToolResult("Unable to look up orders right now.")
    }

    private fun trimHistory(history: MutableList<AiMessage>) {
        val excess = history.size - ChatbotAiConstants.MAX_HISTORY_SIZE
        if (excess > 0) repeat(excess) { history.removeAt(0) }
    }
}