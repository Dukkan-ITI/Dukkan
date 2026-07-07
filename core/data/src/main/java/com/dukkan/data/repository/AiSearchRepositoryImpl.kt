package com.dukkan.data.repository

import android.util.Log
import com.dukkan.data.mapper.toAiToolResponse
import com.dukkan.data.mapper.toClarificationRequest
import com.dukkan.data.mapper.toShopifySearchToolArgs
import com.dukkan.data.source.OllamaChatResponse
import com.dukkan.data.source.OllamaRemoteSource
import com.dukkan.data.util.AiSearchConstants.ASK_CLARIFYING_QUESTION
import com.dukkan.data.util.AiSearchConstants.CACHE_MAX_ENTRIES
import com.dukkan.data.util.AiSearchConstants.CACHE_TTL_MS
import com.dukkan.data.util.AiSearchConstants.MAX_CLARIFICATIONS
import com.dukkan.data.util.AiSearchConstants.MAX_SHOPIFY_CALLS
import com.dukkan.data.util.AiSearchConstants.MAX_TURNS
import com.dukkan.data.util.AiSearchConstants.SEARCH_SHOPIFY_PRODUCTS
import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.AiSearchError
import com.dukkan.domain.repository.AiSearchRepository
import com.dukkan.domain.repository.SearchRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject

@Singleton
class AiSearchRepositoryImpl @Inject constructor(
    private val ollamaRemoteSource: OllamaRemoteSource,
    private val searchRepository: SearchRepository
) : AiSearchRepository {

    private val sessions = ConcurrentHashMap<String, SearchSession>()
    private val searchCache = SearchCache(CACHE_MAX_ENTRIES, CACHE_TTL_MS)

    override fun startSearch(query: String): Flow<AgenticSearchResult> = flow {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@flow

        val cached = searchCache.get(trimmed)
        if (cached != null) {
            emit(cached)
            return@flow
        }

        val sessionId = UUID.randomUUID().toString()
        val session = SearchSession(messages = ollamaRemoteSource.startMessages())
        session.messages += ollamaRemoteSource.userMessage(trimmed)
        session.lastQuery = trimmed
        sessions[sessionId] = session

        val responseResult = executeWithRetry {
            ollamaRemoteSource.sendChat(session.messages)
        }

        responseResult.fold(
            onSuccess = { response ->
                emitAllResults(sessionId, session, response, trimmed, initialQuery = trimmed)
            },
            onFailure = { error ->
                Log.e("AiSearchRepository", "startSearch API error", error)
                emit(AgenticSearchResult.Error(mapToAiSearchError(error)))
                sessions.remove(sessionId)
            }
        )
    }

    override fun resumeWithAnswer(sessionId: String, answer: String): Flow<AgenticSearchResult> = flow {
        val session = sessions[sessionId]
        if (session == null) {
            emit(AgenticSearchResult.Error(AiSearchError.Unknown, "This AI search session expired. Please search again."))
            return@flow
        }

        session.messages += ollamaRemoteSource.userMessage(answer.trim())

        val responseResult = executeWithRetry {
            ollamaRemoteSource.sendChat(session.messages)
        }

        responseResult.fold(
            onSuccess = { response ->
                emitAllResults(sessionId, session, response, session.lastQuery, initialQuery = null)
            },
            onFailure = { error ->
                Log.e("AiSearchRepository", "resumeWithAnswer API error", error)
                emit(AgenticSearchResult.Error(mapToAiSearchError(error)))
            }
        )
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AgenticSearchResult>.emitAllResults(
        sessionId: String,
        session: SearchSession,
        firstResponse: OllamaChatResponse,
        fallbackQuery: String,
        initialQuery: String?
    ) {
        var retainSession = false
        try {
            var response = firstResponse
            var turns = 0

            while (turns < MAX_TURNS) {
                turns += 1
                val functionCall = response.toolCall

                when (functionCall?.name) {
                    ASK_CLARIFYING_QUESTION -> {
                        if (session.clarificationCount >= MAX_CLARIFICATIONS) {
                            emit(AgenticSearchResult.Error(AiSearchError.ClarificationLimitReached))
                        } else {
                            val clarification = functionCall.args.toClarificationRequest(sessionId)
                            session.clarificationCount += 1
                            session.messages += ollamaRemoteSource.assistantMessage(clarification.question)
                            emit(AgenticSearchResult.AwaitingClarification(clarification))
                            retainSession = true
                        }
                        return
                    }

                    SEARCH_SHOPIFY_PRODUCTS -> {
                        if (session.shopifyCalls >= MAX_SHOPIFY_CALLS) {
                            emit(AgenticSearchResult.Error(AiSearchError.MaxStepsReached))
                            return
                        }

                        session.shopifyCalls += 1
                        session.messages += response.assistantMessage
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
                                session.messages += ollamaRemoteSource.toolMessage(
                                    functionName = functionCall.name,
                                    response = result.products.toAiToolResponse(result.totalCount)
                                )

                                val finalResponseResult = executeWithRetry {
                                    ollamaRemoteSource.sendChat(session.messages)
                                }

                                finalResponseResult.fold(
                                    onSuccess = { aiResponse ->
                                        aiResponse.content
                                            ?.takeIf { it.isNotBlank() }
                                            ?.let { session.messages += ollamaRemoteSource.assistantMessage(it) }

                                        val successResult = AgenticSearchResult.Success(
                                            query = args.query,
                                            products = result.products,
                                            totalCount = result.totalCount,
                                            message = aiResponse.content
                                        )
                                        if (initialQuery != null) {
                                            searchCache.put(initialQuery, successResult)
                                        }
                                        emit(successResult)
                                    },
                                    onFailure = { error ->
                                        Log.e("AiSearchRepository", "send tool response API error", error)
                                        emit(AgenticSearchResult.Error(mapToAiSearchError(error)))
                                    }
                                )
                            },
                            onFailure = { error ->
                                emit(AgenticSearchResult.Error(AiSearchError.SearchFailed, error.localizedMessage))
                                return
                            }
                        )
                        return
                    }

                    else -> {
                        val fallback = fallbackQuery.ifBlank { session.lastQuery }
                        searchRepository.searchProducts(query = fallback, first = 20, after = null)
                            .fold(
                                onSuccess = { result ->
                                    response.content
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let { session.messages += ollamaRemoteSource.assistantMessage(it) }

                                    val successResult = AgenticSearchResult.Success(
                                        query = fallback,
                                        products = result.products,
                                        totalCount = result.totalCount,
                                        message = response.content
                                    )
                                    if (initialQuery != null) {
                                        searchCache.put(initialQuery, successResult)
                                    }
                                    emit(successResult)
                                },
                                onFailure = { error ->
                                    emit(AgenticSearchResult.Error(AiSearchError.SearchFailed, error.localizedMessage))
                                }
                            )
                        return
                    }
                }
            }

            emit(AgenticSearchResult.Error(AiSearchError.TimeoutExceeded))
        } finally {
            if (!retainSession) {
                sessions.remove(sessionId)
            }
        }
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
                if (!isTransientError(e) || attempt == maxRetries) {
                    break
                }
                kotlinx.coroutines.delay(1000L * (attempt + 1))
            }
        }
        return Result.failure(lastException ?: Exception("Unknown error"))
    }

    private fun isTransientError(e: Throwable): Boolean {
        val msg = e.message ?: return false
        return msg.contains("429") || msg.contains("500") || msg.contains("502") ||
            msg.contains("503") || msg.contains("timeout", ignoreCase = true) ||
            msg.contains("quota", ignoreCase = true) || msg.contains("rate limit", ignoreCase = true) ||
            msg.contains("UnknownHostException")
    }

    private fun mapToAiSearchError(e: Throwable): AiSearchError {
        val msg = e.message ?: ""
        if (msg.contains("OLLAMA_API_KEY_MISSING") || msg.contains("401") || msg.contains("403")) {
            return AiSearchError.ConfigurationMissing
        }
        if (msg.contains("429") || msg.contains("quota", ignoreCase = true) || msg.contains("rate limit", ignoreCase = true)) {
            return AiSearchError.RateLimited
        }
        if (msg.contains("timeout", ignoreCase = true) || msg.contains("502") || msg.contains("503") ||
            msg.contains("504") || msg.contains("UnknownHostException")
        ) {
            return AiSearchError.Timeout
        }
        return AiSearchError.Unknown
    }

    private data class SearchSession(
        val messages: MutableList<JsonObject>,
        var lastQuery: String = "",
        var clarificationCount: Int = 0,
        var shopifyCalls: Int = 0
    )

    private class SearchCache(private val maxEntries: Int, private val ttlMs: Long) {
        private val cache = object : java.util.LinkedHashMap<String, CacheEntry>(maxEntries, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
                return size > maxEntries
            }
        }

        @Synchronized
        fun get(key: String): AgenticSearchResult.Success? {
            val entry = cache[key] ?: return null
            if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
                cache.remove(key)
                return null
            }
            return entry.result
        }

        @Synchronized
        fun put(key: String, value: AgenticSearchResult.Success) {
            cache[key] = CacheEntry(value, System.currentTimeMillis())
        }

        private data class CacheEntry(val result: AgenticSearchResult.Success, val timestamp: Long)
    }
}
