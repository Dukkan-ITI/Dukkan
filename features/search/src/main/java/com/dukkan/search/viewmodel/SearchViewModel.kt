package com.dukkan.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.search.uistate.SearchUiState
import com.dukkan.search.uistate.UiText
import com.dukkan.domain.model.AgenticSearchResult
import com.dukkan.domain.model.AiSearchError
import com.dukkan.domain.model.SearchFilter
import com.dukkan.domain.usecase.search.PredictiveSearchUseCase
import com.dukkan.domain.usecase.search.AgenticSearchUseCase
import com.dukkan.domain.usecase.search.ResumeSearchClarificationUseCase
import com.dukkan.domain.usecase.search.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dukkan.search.R
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val predictiveSearchUseCase: PredictiveSearchUseCase,
    private val agenticSearchUseCase: AgenticSearchUseCase,
    private val resumeSearchClarificationUseCase: ResumeSearchClarificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryInputFlow = MutableStateFlow("")
    private var loadMoreJob: Job? = null
    private var agenticSearchJob: Job? = null

    init {
        _queryInputFlow
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { query -> fetchPredictive(query) }
            .launchIn(viewModelScope)

        _queryInputFlow
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.isBlank() }
            .onEach { clearPredictive() }
            .launchIn(viewModelScope)
    }

    fun onQueryInputChanged(text: String) {
        _uiState.update { it.copy(queryInput = text) }
        _queryInputFlow.value = text.trim()
    }

    fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        _uiState.update {
            it.copy(
                submittedQuery = trimmed,
                searchResults = emptyList(),
                totalCount = 0,
                hasNextPage = false,
                endCursor = null,
                isSearchLoading = true,
                isAiSearchLoading = false,
                aiMessage = null,
                clarificationQuestion = null,
                clarificationSessionId = null,
                clarificationAnswerInput = "",
                error = null,
                predictiveProducts = emptyList(),
                predictiveCollections = emptyList(),
                isPredictiveLoading = false
            )
        }
        viewModelScope.launch {
            searchProductsUseCase(
                query = trimmed,
                first = 20,
                after = null,
                filters = _uiState.value.activeFilters
            )
                .onSuccess { result ->
                    _uiState.update { s ->
                        s.copy(
                            searchResults = result.products,
                            totalCount = result.totalCount,
                            hasNextPage = result.pageInfo.hasNextPage,
                            endCursor = result.pageInfo.endCursor,
                            isSearchLoading = false,
                            availableVendors = result.products.map { it.vendor }.filter { it.isNotBlank() }.distinct(),
                            availableProductTypes = result.products.map { it.productType }.filter { it.isNotBlank() }.distinct()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { s ->
                        s.copy(
                            isSearchLoading = false,
                            error = e.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.search_failed)
                        )
                    }
                }
        }
    }

    fun onAiSearchTriggered(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        agenticSearchJob?.cancel()
        agenticSearchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    submittedQuery = trimmed,
                    searchResults = emptyList(),
                    totalCount = 0,
                    hasNextPage = false,
                    endCursor = null,
                    isSearchLoading = false,
                    isLoadingMore = false,
                    isAiSearchLoading = true,
                    isAiError = false,
                    aiMessage = null,
                    clarificationQuestion = null,
                    clarificationSessionId = null,
                    clarificationAnswerInput = "",
                    error = null,
                    predictiveProducts = emptyList(),
                    predictiveCollections = emptyList(),
                    isPredictiveLoading = false
                )
            }

            val timeoutJob = launch {
                kotlinx.coroutines.delay(8000)
                _uiState.update { state ->
                    if (state.isAiSearchLoading) {
                        state.copy(aiMessage = UiText.StringResource(R.string.search_ai_still_searching))
                    } else state
                }
            }

            runCatching {
                agenticSearchUseCase(trimmed).collect(::handleAgenticSearchResult)
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                timeoutJob.cancel()
                _uiState.update { state ->
                    state.copy(
                        isAiSearchLoading = false,
                        isAiError = true,
                        aiMessage = error.localizedMessage?.let { msg -> UiText.DynamicString(msg) } ?: UiText.StringResource(R.string.search_failed),
                        clarificationQuestion = null,
                        clarificationSessionId = null
                    )
                }
            }
            timeoutJob.cancel()
        }
    }

    fun onClarificationAnswerChanged(answer: String) {
        _uiState.update { it.copy(clarificationAnswerInput = answer) }
    }

    fun onClarificationAnswered(voiceAnswer: String? = null) {
        val state = _uiState.value
        val answer = voiceAnswer?.trim() ?: state.clarificationAnswerInput.trim()
        if (answer.isBlank()) return

        val sessionId = state.clarificationSessionId
        if (sessionId == null) {
            onAiSearchTriggered(answer)
            return
        }

        agenticSearchJob?.cancel()
        agenticSearchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiSearchLoading = true,
                    isAiError = false,
                    clarificationQuestion = null,
                    clarificationAnswerInput = "",
                    error = null
                )
            }

            runCatching {
                resumeSearchClarificationUseCase(sessionId, answer).collect(::handleAgenticSearchResult)
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                _uiState.update {
                    it.copy(
                        isAiSearchLoading = false,
                        isAiError = true,
                        aiMessage = error.localizedMessage?.let { msg -> UiText.DynamicString(msg) } ?: UiText.StringResource(R.string.search_failed),
                        clarificationQuestion = null,
                        clarificationSessionId = null
                    )
                }
            }
        }
    }

    private fun handleAgenticSearchResult(result: AgenticSearchResult) {
        when (result) {
            is AgenticSearchResult.AwaitingClarification -> {
                _uiState.update {
                    it.copy(
                        isAiSearchLoading = false,
                        clarificationQuestion = UiText.DynamicString(result.request.question),
                        clarificationSessionId = result.request.sessionId,
                        aiMessage = UiText.DynamicString(result.request.question),
                        error = null
                    )
                }
            }

            is AgenticSearchResult.Success -> {
                _uiState.update {
                    it.copy(
                        submittedQuery = result.query,
                        queryInput = result.query,
                        searchResults = result.products,
                        totalCount = result.totalCount,
                        hasNextPage = false,
                        endCursor = null,
                        isAiSearchLoading = false,
                        aiMessage = result.message?.let { UiText.DynamicString(it) },
                        clarificationQuestion = null,
                        clarificationSessionId = null,
                        clarificationAnswerInput = "",
                        error = null,
                        availableVendors = result.products.map { product -> product.vendor }.filter { vendor -> vendor.isNotBlank() }.distinct(),
                        availableProductTypes = result.products.map { product -> product.productType }.filter { type -> type.isNotBlank() }.distinct()
                    )
                }
            }

            is AgenticSearchResult.Error -> {
                val messageResource = when (result.errorType) {
                    AiSearchError.RateLimited -> UiText.StringResource(R.string.error_ai_rate_limited)
                    AiSearchError.Timeout -> UiText.StringResource(R.string.error_ai_timeout)
                    AiSearchError.ConfigurationMissing -> UiText.StringResource(R.string.error_ai_configuration_missing)
                    AiSearchError.ClarificationLimitReached -> UiText.StringResource(R.string.error_ai_clarification_limit)
                    AiSearchError.MaxStepsReached -> UiText.StringResource(R.string.error_ai_max_steps)
                    AiSearchError.TimeoutExceeded -> UiText.StringResource(R.string.error_ai_timeout_exceeded)
                    AiSearchError.SearchFailed -> result.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_ai_search_failed)
                    AiSearchError.Unknown -> result.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_ai_unknown)
                }

                _uiState.update {
                    it.copy(
                        isAiSearchLoading = false,
                        isAiError = true,
                        aiMessage = messageResource,
                        clarificationQuestion = null,
                        clarificationSessionId = null
                    )
                }
            }
        }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (!state.hasNextPage || state.isLoadingMore || state.submittedQuery.isBlank()) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            searchProductsUseCase(
                query = state.submittedQuery,
                first = 20,
                after = state.endCursor,
                filters = state.activeFilters
            )
                .onSuccess { result ->
                    _uiState.update { s ->
                        val newResults = s.searchResults + result.products
                        s.copy(
                            searchResults = newResults,
                            totalCount = result.totalCount,
                            hasNextPage = result.pageInfo.hasNextPage,
                            endCursor = result.pageInfo.endCursor,
                            isLoadingMore = false,
                            availableVendors = newResults.map { it.vendor }.filter { it.isNotBlank() }.distinct(),
                            availableProductTypes = newResults.map { it.productType }.filter { it.isNotBlank() }.distinct()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { s ->
                        s.copy(
                            isLoadingMore = false,
                            error = e.localizedMessage?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.search_load_more_failed)
                        )
                    }
                }
        }
    }

    private suspend fun fetchPredictive(query: String) {
        _uiState.update { it.copy(isPredictiveLoading = true) }
        predictiveSearchUseCase(query)
            .onSuccess { result ->
                _uiState.update {
                    it.copy(
                        predictiveProducts = result.products,
                        predictiveCollections = result.collections,
                        isPredictiveLoading = false
                    )
                }
            }
            .onFailure {
                _uiState.update { s -> s.copy(isPredictiveLoading = false) }
            }
    }

    private fun clearPredictive() {
        _uiState.update {
            it.copy(
                predictiveProducts = emptyList(),
                predictiveCollections = emptyList(),
                isPredictiveLoading = false
            )
        }
    }

    fun onOpenFilters() {
        _uiState.update { it.copy(isFilterSheetOpen = true) }
    }

    fun onDismissFilters() {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
    }

    fun onApplyFilters(filters: SearchFilter) {
        _uiState.update {
            it.copy(
                activeFilters = filters,
                isFilterSheetOpen = false,
                searchResults = emptyList(),
                endCursor = null,
                hasNextPage = false
            )
        }
        val currentQuery = _uiState.value.submittedQuery
        if (currentQuery.isNotBlank()) {
            onSearchSubmitted(currentQuery)
        }
    }

    fun onClearFilters() {
        onApplyFilters(SearchFilter())
    }

    fun onCancelAiSearch() {
        agenticSearchJob?.cancel()
        _uiState.update {
            it.copy(
                isAiSearchLoading = false,
                isAiError = false,
                aiMessage = null,
                clarificationQuestion = null,
                clarificationSessionId = null,
                clarificationAnswerInput = ""
            )
        }
    }

    fun onRetryAiSearch() {
        val state = _uiState.value
        val sessionId = state.clarificationSessionId
        val answer = state.clarificationAnswerInput.trim()
        val query = state.submittedQuery

        if (sessionId != null && answer.isNotBlank()) {
            onClarificationAnswered()
        } else if (query.isNotBlank()) {
            onAiSearchTriggered(query)
        }
    }

    fun onAiVoiceSearchSubmitted(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        _uiState.update { 
            it.copy(
                lastSearchSource = com.dukkan.search.uistate.SearchSource.VOICE,
                queryInput = trimmed,
                submittedQuery = trimmed
            ) 
        }
        onAiSearchTriggered(trimmed)
    }

    fun onVoiceInputReceived(text: String) {
        val state = _uiState.value
        if (state.clarificationSessionId != null) {
            onClarificationAnswerChanged(text)
            onClarificationAnswered(text)
        } else {
            onAiVoiceSearchSubmitted(text)
        }
    }

    fun setListeningState(isListening: Boolean) {
        _uiState.update { it.copy(isListening = isListening) }
    }
}
