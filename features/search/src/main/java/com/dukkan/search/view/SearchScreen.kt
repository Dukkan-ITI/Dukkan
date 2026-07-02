package com.dukkan.search.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.search.R
import com.dukkan.search.components.PredictiveSuggestionsDropdown
import com.dukkan.search.components.ProductResultItem
import com.dukkan.search.components.SearchBar
import com.dukkan.search.components.SearchEmptyState
import com.dukkan.search.components.SearchLoadingState
import com.dukkan.search.uiState.SearchUiState
import com.dukkan.search.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToProductDetails: (productId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreenContent(
        modifier = modifier,
        uiState = uiState,
        onQueryChange = viewModel::onQueryInputChanged,
        onSearchSubmit = viewModel::onSearchSubmitted,
        onLoadMore = viewModel::onLoadMore,
        onProductClick = { product ->
            viewModel.onSearchSubmitted(product.title)
            onNavigateToProductDetails(product.id)
        },
        onCollectionClick = { collection ->
            viewModel.onSearchSubmitted(collection.title)
        },
        onOpenFilters = viewModel::onOpenFilters,
        onDismissFilters = viewModel::onDismissFilters,
        onApplyFilters = viewModel::onApplyFilters,
        onClearFilters = viewModel::onClearFilters
    )
}

@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onLoadMore: () -> Unit,
    onProductClick: (com.msayeh.domain.model.SearchProduct) -> Unit,
    onCollectionClick: (com.msayeh.domain.model.SearchCollection) -> Unit,
    onOpenFilters: () -> Unit,
    onDismissFilters: () -> Unit,
    onApplyFilters: (com.msayeh.domain.model.SearchFilter) -> Unit,
    onClearFilters: () -> Unit
) {
    val listState = rememberLazyListState()

    // Trigger load-more when near end
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && total > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    val showDropdown = uiState.queryInput.isNotBlank() &&
        uiState.submittedQuery != uiState.queryInput &&
        (uiState.predictiveProducts.isNotEmpty() || uiState.predictiveCollections.isNotEmpty())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search bar + dropdown in a Box to allow overlay
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        SearchBar(
                            query = uiState.queryInput,
                            onQueryChange = onQueryChange,
                            onSearchSubmit = onSearchSubmit,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material3.IconButton(
                            onClick = onOpenFilters,
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                containerColor = if (uiState.activeFilters.isActive) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                            )
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.List,
                                contentDescription = "Filters",
                                tint = if (uiState.activeFilters.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (uiState.activeFilters.isActive) {
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = uiState.activeFilters
                            if (filters.minPrice != null || filters.maxPrice != null) {
                                item {
                                    androidx.compose.material3.InputChip(
                                        selected = true,
                                        onClick = { onApplyFilters(filters.copy(minPrice = null, maxPrice = null)) },
                                        label = { Text("Price: ${filters.minPrice ?: 0} - ${filters.maxPrice ?: "Any"}") },
                                        trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Clear") }
                                    )
                                }
                            }
                            if (filters.availableOnly) {
                                item {
                                    androidx.compose.material3.InputChip(
                                        selected = true,
                                        onClick = { onApplyFilters(filters.copy(availableOnly = false)) },
                                        label = { Text("In Stock") },
                                        trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Clear") }
                                    )
                                }
                            }
                            items(filters.vendors) { vendor ->
                                androidx.compose.material3.InputChip(
                                    selected = true,
                                    onClick = { onApplyFilters(filters.copy(vendors = filters.vendors - vendor)) },
                                    label = { Text(vendor) },
                                    trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Clear") }
                                )
                            }
                            items(filters.productTypes) { type ->
                                androidx.compose.material3.InputChip(
                                    selected = true,
                                    onClick = { onApplyFilters(filters.copy(productTypes = filters.productTypes - type)) },
                                    label = { Text(type) },
                                    trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Clear") }
                                )
                            }
                        }
                    }

                    if (showDropdown) {
                        PredictiveSuggestionsDropdown(
                            products = uiState.predictiveProducts,
                            collections = uiState.predictiveCollections,
                            onProductClick = onProductClick,
                            onCollectionClick = onCollectionClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Results area
            when {
                uiState.isSearchLoading -> SearchLoadingState()

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                uiState.submittedQuery.isNotBlank() && uiState.searchResults.isEmpty() -> {
                    SearchEmptyState(query = uiState.submittedQuery)
                }

                uiState.submittedQuery.isBlank() && !showDropdown -> {
                    SearchEmptyState(query = "")
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Results header
                        if (uiState.submittedQuery.isNotBlank()) {
                            item {
                                Text(
                                    text = stringResource(
                                        R.string.search_results_count,
                                        uiState.totalCount,
                                        uiState.submittedQuery
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        itemsIndexed(
                            items = uiState.searchResults,
                            key = { _, product -> product.id }
                        ) { index, product ->
                            ProductResultItem(
                                product = product,
                                onClick = { onProductClick(product) }
                            )
                            if (index < uiState.searchResults.lastIndex) {
                                Divider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // Load-more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .padding(4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isFilterSheetOpen) {
            com.dukkan.search.components.FilterBottomSheet(
                initialFilters = uiState.activeFilters,
                searchResults = uiState.searchResults,
                onDismissRequest = onDismissFilters,
                onApplyFilters = onApplyFilters,
                onClearFilters = onClearFilters
            )
        }
    }
}
