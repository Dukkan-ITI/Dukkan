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
import com.dukkan.data.ai.handler.SearchAiConstants
import com.dukkan.data.ai.mapper.errorToolResult
import com.dukkan.data.ai.mapper.orderNumberArg
import com.dukkan.data.ai.mapper.successToolResult
import com.dukkan.data.ai.mapper.toAiToolResponse
import com.dukkan.data.ai.mapper.toShopifySearchToolArgs
import com.dukkan.data.ai.mapper.unknownToolResult
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.repository.SearchRepository
import com.dukkan.domain.usecase.cart.AddToCartUseCase
import com.dukkan.domain.usecase.cart.RemoveFromCartUseCase
import com.dukkan.domain.usecase.cart.GetCartUseCase
import com.dukkan.domain.usecase.favorite.AddFavoriteUseCase
import com.dukkan.domain.usecase.favorite.RemoveFavoriteUseCase
import com.dukkan.domain.usecase.order.GetAllOrdersUseCase
import com.dukkan.domain.usecase.order.GetRecentOrdersUseCase
import com.dukkan.domain.usecase.product.GetProductByIdUseCase
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class ChatbotTaskInput(
    val sessionId: String,
    val message: String
)

data class ChatbotTaskResult(
    val reply: String,
    val products: List<SearchProduct> = emptyList(),
    val orders: List<Order> = emptyList()
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
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val firebaseAuth: FirebaseAuth
) : AiTask<ChatbotTaskInput, ChatbotTaskResult> {

    override val taskId: String = "ChatbotTask"

    private val sessions = mutableMapOf<String, MutableList<AiMessage>>()
    private val lastProducts = mutableMapOf<String, MutableList<SearchProduct>>()
    private val lastOrders = mutableMapOf<String, MutableList<Order>>()

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
        ),
        ToolDefinition(
            name = ChatbotAiConstants.ADD_TO_FAVORITE,
            description = "Save a product to the shopper's favorites/wishlist. Requires login.",
            properties = mapOf(
                "productId" to ToolParameter(ToolParameterType.String, "The unique Shopify ID of the product.")
            ),
            required = listOf("productId")
        ),
        ToolDefinition(
            name = ChatbotAiConstants.REMOVE_FROM_FAVORITE,
            description = "Remove a product from the shopper's favorites/wishlist. Requires login.",
            properties = mapOf(
                "productId" to ToolParameter(ToolParameterType.String, "The unique Shopify ID of the product.")
            ),
            required = listOf("productId")
        ),
        ToolDefinition(
            name = ChatbotAiConstants.ADD_TO_CART,
            description = "Add a specific product variant to the user's shopping cart. Requires login.",
            properties = mapOf(
                "variantId" to ToolParameter(ToolParameterType.String, "The Shopify Variant ID.")
            ),
            required = listOf("variantId")
        ),
        ToolDefinition(
            name = ChatbotAiConstants.REMOVE_FROM_CART,
            description = "Remove a product from the shopper's cart. Requires login.",
            properties = mapOf(
                "variantId" to ToolParameter(ToolParameterType.String, "The Shopify Variant ID to remove.")
            ),
            required = listOf("variantId")
        ),
        ToolDefinition(
            name = ChatbotAiConstants.CREATE_ORDER,
            description = "Initiate a Cash on Delivery order for the items currently in the cart. Requires login.",
            properties = emptyMap(),
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
        
        lastProducts[input.sessionId] = mutableListOf()
        lastOrders[input.sessionId] = mutableListOf()

        var turns = 0
        var accumulatedReply = ""

        while (turns < ChatbotAiConstants.MAX_TOOL_TURNS) {
            val request = AiRequest(
                systemInstruction = ChatbotAiConstants.SYSTEM_INSTRUCTION,
                messages = history.toList(),
                tools = tools
            )

            val responseResult = orchestrator.generate(request)
            if (responseResult.isFailure) {
                if (accumulatedReply.isNotBlank()) {
                    return Result.success(createResult(input.sessionId, accumulatedReply))
                }
                history.removeLastOrNull()
                return Result.success(ChatbotTaskResult(reply = ChatbotAiConstants.FALLBACK_REPLY))
            }

            val response = responseResult.getOrThrow()
            val turnText = response.text?.trim().orEmpty()
            
            if (turnText.isNotBlank()) {
                if (accumulatedReply.isNotBlank()) accumulatedReply += "\n"
                accumulatedReply += turnText
            }

            history.add(
                AiMessage(role = AiRole.Model, text = turnText, toolCalls = response.toolCalls)
            )

            val toolCall = response.toolCalls.firstOrNull()
            if (toolCall == null) {
                trimHistory(history)
                val finalReply = accumulatedReply.takeIf { it.isNotBlank() } ?: ChatbotAiConstants.FALLBACK_REPLY
                return Result.success(createResult(input.sessionId, finalReply))
            }

            val toolResult = executeTool(input.sessionId, toolCall)
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
        return Result.success(createResult(input.sessionId, accumulatedReply.takeIf { it.isNotBlank() } ?: ChatbotAiConstants.MAX_STEPS_REPLY))
    }
    
    private fun createResult(sessionId: String, reply: String): ChatbotTaskResult {
        return ChatbotTaskResult(
            reply = reply,
            products = lastProducts[sessionId]?.toList() ?: emptyList(),
            orders = lastOrders[sessionId]?.toList() ?: emptyList()
        )
    }

    private suspend fun executeTool(sessionId: String, toolCall: AiToolCall): JsonElement = when (toolCall.name) {
        SearchAiConstants.SEARCH_SHOPIFY_PRODUCTS -> executeProductSearch(sessionId, toolCall)
        ChatbotAiConstants.GET_ORDER_STATUS -> requireAuth { executeOrderLookup(sessionId, toolCall) }
        ChatbotAiConstants.ADD_TO_FAVORITE -> requireAuth { executeAddFavorite(toolCall) }
        ChatbotAiConstants.REMOVE_FROM_FAVORITE -> requireAuth { executeRemoveFavorite(toolCall) }
        ChatbotAiConstants.ADD_TO_CART -> requireAuth { executeAddToCart(toolCall) }
        ChatbotAiConstants.REMOVE_FROM_CART -> requireAuth { executeRemoveFromCart(toolCall) }
        ChatbotAiConstants.CREATE_ORDER -> requireAuth { executeCreateOrder() }
        else -> unknownToolResult(toolCall.name)
    }

    private inline fun requireAuth(action: () -> JsonElement): JsonElement {
        return if (firebaseAuth.currentUser == null) {
            errorToolResult("Login Required. Please sign in to your Dukkan account to perform this action.")
        } else {
            action()
        }
    }

    private suspend fun executeProductSearch(sessionId: String, toolCall: AiToolCall): JsonElement = try {
        val args = toolCall.arguments.toShopifySearchToolArgs(fallbackQuery = "")
        if (args.query.isBlank()) {
            errorToolResult("No search query was provided.")
        } else {
            val result = searchRepository.searchProducts(
                query = args.query,
                first = 8,
                filters = args.filters
            ).getOrThrow()
            lastProducts[sessionId]?.addAll(result.products)
            result.products.toAiToolResponse(result.totalCount)
        }
    } catch (e: Exception) {
        errorToolResult("Unable to search products right now.")
    }

    private suspend fun executeOrderLookup(sessionId: String, toolCall: AiToolCall): JsonElement = try {
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
        lastOrders[sessionId]?.addAll(matches)
        matches.toAiToolResponse(requestedNumber)
    } catch (e: Exception) {
        errorToolResult("Unable to look up orders right now.")
    }

    private suspend fun executeAddFavorite(toolCall: AiToolCall): JsonElement = try {
        val productId = toolCall.arguments["productId"]?.jsonPrimitive?.contentOrNull
        if (productId == null) {
            errorToolResult("Missing productId.")
        } else {
            val productResult = getProductByIdUseCase(productId)
            if (productResult.isSuccess) {
                val product = productResult.getOrThrow()
                val favoriteProduct = FavoriteProduct(
                    id = product.id,
                    title = product.title,
                    imageUrl = product.featuredImage?.url.orEmpty(),
                    price = product.minPrice.amount.toString(),
                    currencyCode = product.minPrice.currencyCode,
                    rating = product.averageRating,
                    reviewCount = product.reviews.size
                )
                addFavoriteUseCase(favoriteProduct)
                successToolResult("Product added to favorites.")
            } else {
                errorToolResult("Product not found.")
            }
        }
    } catch (e: Exception) {
        errorToolResult("Failed to add to favorites: ${e.message}")
    }

    private suspend fun executeRemoveFavorite(toolCall: AiToolCall): JsonElement = try {
        val productId = toolCall.arguments["productId"]?.jsonPrimitive?.contentOrNull
        if (productId == null) {
            errorToolResult("Missing productId.")
        } else {
            removeFavoriteUseCase(productId)
            successToolResult("Product removed from favorites.")
        }
    } catch (e: Exception) {
        errorToolResult("Failed to remove from favorites: ${e.message}")
    }

    private suspend fun executeAddToCart(toolCall: AiToolCall): JsonElement = try {
        val variantId = toolCall.arguments["variantId"]?.jsonPrimitive?.contentOrNull
        if (variantId == null) {
            errorToolResult("Missing variantId.")
        } else {
            addToCartUseCase(variantId)
            successToolResult("Product added to cart.")
        }
    } catch (e: Exception) {
        errorToolResult("Failed to add to cart: ${e.message}")
    }

    private suspend fun executeRemoveFromCart(toolCall: AiToolCall): JsonElement = try {
        val variantId = toolCall.arguments["variantId"]?.jsonPrimitive?.contentOrNull
        if (variantId == null) {
            errorToolResult("Missing variantId.")
        } else {
            val cart = getCartUseCase()
            val lineItem = cart?.lines?.find { it.merchandise.id == variantId }
            if (lineItem != null) {
                removeFromCartUseCase(lineItem.id)
                successToolResult("Product removed from cart.")
            } else {
                errorToolResult("Product not found in cart.")
            }
        }
    } catch (e: Exception) {
        errorToolResult("Failed to remove from cart: ${e.message}")
    }

    private suspend fun executeCreateOrder(): JsonElement = try {
        val cart = getCartUseCase()
        if (cart == null || cart.isEmpty) {
            errorToolResult("Your cart is empty. Add some products first!")
        } else {
            // We can't safely automate checkout through AI yet due to address/payment requirements.
            // But we can inform the user how to do it.
            successToolResult("To complete your order, please navigate to your Shopping Cart and click the 'Checkout' button. You can then select 'Cash on Delivery' as your payment method.")
        }
    } catch (e: Exception) {
        errorToolResult("Unable to process order request right now.")
    }

    private fun trimHistory(history: MutableList<AiMessage>) {
        val excess = history.size - ChatbotAiConstants.MAX_HISTORY_SIZE
        if (excess > 0) repeat(excess) { history.removeAt(0) }
    }
}
