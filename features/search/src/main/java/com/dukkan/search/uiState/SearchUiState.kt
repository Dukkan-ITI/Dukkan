package com.dukkan.search.uistate

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dukkan.domain.model.SearchCollection
import com.dukkan.domain.model.SearchProduct

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}

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
    val error: UiText? = null,

    // Agentic AI search - explicit button-driven flow
    val isAiSearchLoading: Boolean = false,
    val isAiError: Boolean = false,
    val aiMessage: UiText? = null,
    val clarificationQuestion: UiText? = null,
    val clarificationSessionId: String? = null,
    val clarificationAnswerInput: String = "",

    // Filter state
    val activeFilters: com.dukkan.domain.model.SearchFilter = com.dukkan.domain.model.SearchFilter(),
    val isFilterSheetOpen: Boolean = false,
    val availableVendors: List<String> = emptyList(),
    val availableProductTypes: List<String> = emptyList(),

    // Voice search
    val isListening: Boolean = false,
    val lastSearchSource: SearchSource = SearchSource.TYPED
)

enum class SearchSource {
    TYPED, VOICE
}
