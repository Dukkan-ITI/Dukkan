package com.dukkan.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.search.uiState.SearchUiState
import com.dukkan.domain.usecase.search.PredictiveSearchUseCase
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
import android.content.Context
import com.dukkan.search.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val predictiveSearchUseCase: PredictiveSearchUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryInputFlow = MutableStateFlow("")
    private var loadMoreJob: Job? = null

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
                            error = e.localizedMessage ?: context.getString(R.string.search_failed)
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
                            error = e.localizedMessage ?: context.getString(R.string.search_load_more_failed)
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

    fun onApplyFilters(filters: com.dukkan.domain.model.SearchFilter) {
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
        onApplyFilters(com.dukkan.domain.model.SearchFilter())
    }
}
