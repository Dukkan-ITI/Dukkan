package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toClarificationRequest
import com.dukkan.data.mapper.toGeminiToolResponse
import com.dukkan.data.mapper.toShopifySearchToolArgs
import com.dukkan.data.source.GeminiRemoteSource
import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.SearchIntent
import com.dukkan.domain.repository.GeminiRepository
import com.dukkan.domain.repository.SearchRepository
import com.google.firebase.ai.Chat
import com.google.firebase.ai.type.GenerateContentResponse
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import com.dukkan.data.util.GeminiConstants.ASK_CLARIFYING_QUESTION
import com.dukkan.data.util.GeminiConstants.MAX_CLARIFICATIONS
import com.dukkan.data.util.GeminiConstants.MAX_SHOPIFY_CALLS
import com.dukkan.data.util.GeminiConstants.MAX_TURNS
import com.dukkan.data.util.GeminiConstants.SEARCH_SHOPIFY_PRODUCTS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val geminiRemoteSource: GeminiRemoteSource,
    private val searchRepository: SearchRepository
) : GeminiRepository {

    private val sessions = ConcurrentHashMap<String, SearchSession>()
    private val searchCache = ConcurrentHashMap<String, AgenticSearchResult.Success>()

    override fun startSearch(query: String): Flow<AgenticSearchResult> = flow {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@flow

        val cached = searchCache[trimmed]
        if (cached != null) {
            emit(cached)
            return@flow
        }

        val sessionId = UUID.randomUUID().toString()
        val session = SearchSession(chat = geminiRemoteSource.startChat())
        sessions[sessionId] = session

        val responseResult = executeWithRetry {
            geminiRemoteSource.sendUserMessage(session.chat, trimmed)
        }

        responseResult.fold(
            onSuccess = { response ->
                emitAllResults(sessionId, session, response, trimmed, initialQuery = trimmed)
            },
            onFailure = { error ->
                Log.e("GeminiRepository", "startSearch API error", error)
                emit(AgenticSearchResult.Error(mapToFriendlyError(error)))
                sessions.remove(sessionId)
            }
        )
    }

    override fun resumeWithAnswer(sessionId: String, answer: String): Flow<AgenticSearchResult> = flow {
        val session = sessions[sessionId]
        if (session == null) {
            emit(AgenticSearchResult.Error("This AI search session expired. Please search again."))
            return@flow
        }

        val responseResult = executeWithRetry {
            geminiRemoteSource.sendUserMessage(session.chat, answer.trim())
        }

        responseResult.fold(
            onSuccess = { response ->
                emitAllResults(sessionId, session, response, session.lastQuery, initialQuery = null)
            },
            onFailure = { error ->
                Log.e("GeminiRepository", "resumeWithAnswer API error", error)
                emit(AgenticSearchResult.Error(mapToFriendlyError(error)))
            }
        )
    }

    override suspend fun interpretQueryOnce(query: String): SearchIntent =
        SearchIntent(query = query.trim())

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AgenticSearchResult>.emitAllResults(
        sessionId: String,
        session: SearchSession,
        firstResponse: GenerateContentResponse,
        fallbackQuery: String,
        initialQuery: String?
    ) {
        var response = firstResponse
        var turns = 0

        while (turns < MAX_TURNS) {
            turns += 1
            val functionCall = response.functionCalls.firstOrNull()

            when (functionCall?.name) {
                ASK_CLARIFYING_QUESTION -> {
                    if (session.clarificationCount >= MAX_CLARIFICATIONS) {
                        emit(AgenticSearchResult.Error("I need a little more detail. Please try a more specific search."))
                    } else {
                        session.clarificationCount += 1
                        emit(AgenticSearchResult.AwaitingClarification(functionCall.args.toClarificationRequest(sessionId)))
                    }
                    return
                }

                SEARCH_SHOPIFY_PRODUCTS -> {
                    if (session.shopifyCalls >= MAX_SHOPIFY_CALLS) {
                        emit(AgenticSearchResult.Error("Search took too many steps. Please try a more specific request."))
                        return
                    }

                    session.shopifyCalls += 1
                    val args = functionCall.args.toShopifySearchToolArgs(fallbackQuery)
                    session.lastQuery = args.query

                    val shopifyResult = searchRepository.searchProducts(
                        query = args.query,
                        first = 20,
                        after = null,
                        filters = args.filters
                    )
                    
                    shopifyResult.fold(
                        onSuccess = { result ->
                            val fnResponseResult = executeWithRetry {
                                geminiRemoteSource.sendFunctionResponse(
                                    chat = session.chat,
                                    functionName = functionCall.name,
                                    response = result.products.toGeminiToolResponse(result.totalCount)
                                )
                            }
                            
                            fnResponseResult.fold(
                                onSuccess = { geminiResp ->
                                    val successResult = AgenticSearchResult.Success(
                                        query = args.query,
                                        products = result.products,
                                        totalCount = result.totalCount,
                                        message = geminiResp.text
                                    )
                                    if (initialQuery != null) {
                                        searchCache[initialQuery] = successResult
                                    }
                                    emit(successResult)
                                },
                                onFailure = { error ->
                                    Log.e("GeminiRepository", "sendFunctionResponse API error", error)
                                    emit(AgenticSearchResult.Error(mapToFriendlyError(error)))
                                }
                            )
                        },
                        onFailure = { error ->
                            emit(
                                AgenticSearchResult.Error(
                                    error.localizedMessage ?: "AI search could not reach Shopify search."
                                )
                            )
                            return
                        }
                    )
                    sessions.remove(sessionId)
                    return
                }

                else -> {
                    val fallback = fallbackQuery.ifBlank { session.lastQuery }
                    searchRepository.searchProducts(query = fallback, first = 20, after = null)
                        .fold(
                            onSuccess = { result ->
                                val successResult = AgenticSearchResult.Success(
                                    query = fallback,
                                    products = result.products,
                                    totalCount = result.totalCount,
                                    message = response.text
                                )
                                if (initialQuery != null) {
                                    searchCache[initialQuery] = successResult
                                }
                                emit(successResult)
                            },
                            onFailure = { error ->
                                emit(AgenticSearchResult.Error(error.localizedMessage ?: "AI search failed."))
                            }
                        )
                    sessions.remove(sessionId)
                    return
                }
            }
        }

        emit(AgenticSearchResult.Error("AI search took too long. Please try again."))
        sessions.remove(sessionId)
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 2,
        block: suspend () -> T
    ): Result<T> {
        var lastException: Throwable? = null
        for (attempt in 0..maxRetries) {
            try {
                return Result.success(block())
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                val msg = e.message ?: ""
                val isTransient = msg.contains("429") || msg.contains("500") || msg.contains("503") || msg.contains("timeout", ignoreCase = true)
                if (!isTransient || attempt == maxRetries) {
                    break
                }
                kotlinx.coroutines.delay(1000L * (attempt + 1))
            }
        }
        return Result.failure(lastException ?: Exception("Unknown error"))
    }

    private fun mapToFriendlyError(e: Throwable): String {
        val msg = e.message ?: ""
        if (msg.contains("429") || msg.contains("quota", ignoreCase = true) || msg.contains("rate limit", ignoreCase = true)) {
            return "Dukkan AI is taking a short break — please try again in a moment."
        }
        if (msg.contains("timeout", ignoreCase = true) || msg.contains("503") || msg.contains("504") || msg.contains("UnknownHostException")) {
            return "Looks like our servers are a bit slow right now. Give it another try!"
        }
        return "Something went wrong on our end. Please try again."
    }

    private data class SearchSession(
        val chat: Chat,
        var lastQuery: String = "",
        var clarificationCount: Int = 0,
        var shopifyCalls: Int = 0
    )
}
