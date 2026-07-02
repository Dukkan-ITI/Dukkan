package com.dukkan.search.uiState

import com.msayeh.domain.model.SearchCollection
import com.msayeh.domain.model.SearchProduct

data class SearchUiState(
    // Predictive/autocomplete — keystroke-driven
    val queryInput: String = "",
    val predictiveProducts: List<SearchProduct> = emptyList(),
    val predictiveCollections: List<SearchCollection> = emptyList(),
    val isPredictiveLoading: Boolean = false,

    // Full search results — submit-driven, paginated
    val submittedQuery: String = "",
    val searchResults: List<SearchProduct> = emptyList(),
    val totalCount: Int = 0,
    val hasNextPage: Boolean = false,
    val endCursor: String? = null,
    val isSearchLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,

    // Filter state
    val activeFilters: com.msayeh.domain.model.SearchFilter = com.msayeh.domain.model.SearchFilter(),
    val isFilterSheetOpen: Boolean = false
)
