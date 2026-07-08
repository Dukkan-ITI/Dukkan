package com.dukkan.data.ai.handler

import android.util.Log
import com.dukkan.ai_agent.contract.AiMessage
import com.dukkan.ai_agent.contract.AiRequest
import com.dukkan.ai_agent.contract.AiRole
import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.ai_agent.contract.ToolDefinition
import com.dukkan.ai_agent.contract.ToolParameter
import com.dukkan.ai_agent.contract.ToolParameterType
import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import com.dukkan.domain.model.ComparisonFeature
import com.dukkan.domain.model.McqQuestion
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductComparisonResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ProductComparisonTaskInput(
    val product1: Product,
    val product2: Product,
    val sessionId: String? = null,
    val answer: String? = null
)

@Singleton
class ProductComparisonTask @Inject constructor() :
    AiTask<ProductComparisonTaskInput, ProductComparisonResult> {
    override val taskId: String = "ProductComparisonTask"

    private val sessions = mutableMapOf<String, MutableList<AiMessage>>()

    override suspend fun execute(
        input: ProductComparisonTaskInput,
        orchestrator: AiAgentOrchestrator
    ): Result<ProductComparisonResult> {
        val sessionId = input.sessionId ?: UUID.randomUUID().toString()
        val history = sessions.getOrPut(sessionId) { mutableListOf() }

        if (history.isEmpty()) {
            val p1Text =
                "Product 1: ${input.product1.title} - ${input.product1.description} - Price: ${input.product1.maxPrice}"
            val p2Text =
                "Product 2: ${input.product2.title} - ${input.product2.description} - Price: ${input.product2.maxPrice}"
            history.add(
                AiMessage(
                    role = AiRole.User,
                    text = "I want to compare these two products to decide which one to buy.\n$p1Text\n$p2Text"
                )
            )
        }

        if (!input.answer.isNullOrBlank()) {
            history.add(AiMessage(role = AiRole.User, text = input.answer))
        }

        val tools = listOf(
            ToolDefinition(
                name = "submit_comparison",
                description = "Submit the final structured comparison between the two products. ALL PARAMETERS MUST BE STRINGS.",
                properties = mapOf(
                    "features" to ToolParameter(
                        ToolParameterType.String,
                        "A stringified JSON array of comparison features. You MUST wrap the JSON array in a string. Example: \"[{\\\"featureName\\\":\\\"Brand\\\",\\\"product1Value\\\":\\\"Vans\\\",\\\"product2Value\\\":\\\"Nike\\\"}]\""
                    ),
                    "recommendationText" to ToolParameter(
                        ToolParameterType.String,
                        "A conversational recommendation text for the user."
                    ),
                    "overallWinner" to ToolParameter(
                        ToolParameterType.Number,
                        "The index of the winning product (1 or 2). Omit or set to null if there is no clear winner."
                    )
                ),
                required = listOf("features", "recommendationText")
            ),
            ToolDefinition(
                name = "ask_clarifying_question",
                description = "Ask the user an MCQ question to narrow down their intent or use-case for the product. ALL PARAMETERS MUST BE STRINGS.",
                properties = mapOf(
                    "questionText" to ToolParameter(ToolParameterType.String, "The question to ask the user."),
                    "options" to ToolParameter(ToolParameterType.String, "A stringified JSON array of up to 4 options. Example: \"[\\\"Option 1\\\", \\\"Option 2\\\"]\"")
                ),
                required = listOf("questionText", "options")
            )
        )

        val request = AiRequest(
            systemInstruction = """
                You are an interactive shopping assistant comparing two products. 
                CRITICAL: You MUST ALWAYS respond by calling ONE of the provided tools. DO NOT generate conversational text. The UI depends entirely on your tool calls to render correctly.
                
                If you need to know the user's specific use case to make a decision, you MUST call 'ask_clarifying_question'. 
                - Your questionText will be shown to the user. 
                - Your options MUST be a stringified JSON array (e.g., '["Daily Use", "Skateboarding"]') and will be rendered as interactive buttons.
                
                Once you have enough context, you MUST call 'submit_comparison'.
                - Your features MUST be a stringified JSON array of objects (e.g., '[{"featureName": "Style", "product1Value": "Slip-On", "product2Value": "High Top", "winner": 1}]') and will be rendered as a side-by-side comparison table.
                - Your recommendationText will be shown below the table as the final verdict.
                - Your overallWinner parameter MUST be an integer representing the winning product (1 or 2).
            """.trimIndent(),
            messages = history.toList(),
            tools = tools
        )

        val responseResult = orchestrator.generate(request)
        if (responseResult.isFailure) {
            val exception = responseResult.exceptionOrNull()
            Log.e("ProductComparisonTask", "AI generation failed", exception)
            sessions.remove(sessionId)
            return Result.success(ProductComparisonResult.Error("Failed to communicate with AI"))
        }

        val response = responseResult.getOrThrow()

        history.add(
            AiMessage(
                role = AiRole.Model,
                text = response.text.orEmpty(),
                toolCalls = response.toolCalls
            )
        )

        val toolCall = response.toolCalls.firstOrNull()
        if (toolCall == null) {
            sessions.remove(sessionId)
            return Result.success(ProductComparisonResult.Error("AI did not provide a structured response."))
        }

        return try {
            when (toolCall.name) {
                "submit_comparison" -> {
                    val featuresJsonStr =
                        toolCall.arguments["features"]?.jsonPrimitive?.content ?: "[]"
                    val recommendationText =
                        toolCall.arguments["recommendationText"]?.jsonPrimitive?.content
                    val overallWinner = toolCall.arguments["overallWinner"]?.jsonPrimitive?.intOrNull

                    val parsedArray = Json.parseToJsonElement(featuresJsonStr).jsonArray
                    val features = parsedArray.map { element ->
                        val obj = element.jsonObject
                        ComparisonFeature(
                            featureName = obj["featureName"]?.jsonPrimitive?.content ?: "",
                            product1Value = obj["product1Value"]?.jsonPrimitive?.content ?: "",
                            product2Value = obj["product2Value"]?.jsonPrimitive?.content ?: "",
                            winner = obj["winner"]?.jsonPrimitive?.intOrNull
                        )
                    }
                    sessions.remove(sessionId)
                    Result.success(ProductComparisonResult.Comparison(features, recommendationText, overallWinner))
                }

                "ask_clarifying_question" -> {
                    val questionText =
                        toolCall.arguments["questionText"]?.jsonPrimitive?.content ?: ""
                    val optionsJsonStr =
                        toolCall.arguments["options"]?.jsonPrimitive?.content ?: "[]"

                    val parsedOptions =
                        Json.parseToJsonElement(optionsJsonStr).jsonArray.map { it.jsonPrimitive.content }

                    Result.success(
                        ProductComparisonResult.NeedsMoreInfo(
                            question = McqQuestion(questionText, parsedOptions),
                            sessionId = sessionId
                        )
                    )
                }

                else -> {
                    sessions.remove(sessionId)
                    Result.success(ProductComparisonResult.Error("AI attempted an unknown action: ${toolCall.name}"))
                }
            }
        } catch (e: Exception) {
            sessions.remove(sessionId)
            Result.success(ProductComparisonResult.Error("Failed to parse AI response: ${e.message}"))
        }
    }
}
