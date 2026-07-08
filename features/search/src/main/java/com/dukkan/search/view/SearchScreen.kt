package com.dukkan.search.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.search.R
import com.dukkan.search.components.ClarificationPrompt
import com.dukkan.search.components.PredictiveSuggestionsDropdown
import com.dukkan.search.components.ProductResultItem
import com.dukkan.search.components.SearchBar
import com.dukkan.search.components.SearchEmptyState
import com.dukkan.search.components.SearchLoadingState
import com.dukkan.search.uistate.SearchUiState
import com.dukkan.search.viewmodel.SearchViewModel
import com.dukkan.design_system.components.ChatFab

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToProductDetails: (productId: String) -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startVoiceRecognition(context, viewModel)
            } else {
                android.widget.Toast.makeText(context, context.getString(R.string.search_voice_permission_denied), android.widget.Toast.LENGTH_LONG).show()
            }
        }
    )

    SearchScreenContent(
        modifier = modifier,
        uiState = uiState,
        onQueryChange = viewModel::onQueryInputChanged,
        onSearchSubmit = viewModel::onSearchSubmitted,
        onAiSearchSubmit = viewModel::onAiSearchTriggered,
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
        onClearFilters = viewModel::onClearFilters,
        onClarificationAnswerChange = viewModel::onClarificationAnswerChanged,
        onClarificationAnswerSubmit = viewModel::onClarificationAnswered,
        onRetryAiSearch = viewModel::onRetryAiSearch,
        onCancelAiSearch = viewModel::onCancelAiSearch,
        onNavigateToChat = onNavigateToChat,
        onMicClick = {
            if (!uiState.isListening) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    startVoiceRecognition(context, viewModel)
                } else {
                    speechRecognizerLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    )
}

private fun startVoiceRecognition(context: android.content.Context, viewModel: SearchViewModel) {
    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    val recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
    recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) { viewModel.setListeningState(true) }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { viewModel.setListeningState(false) }
        override fun onError(error: Int) { 
            viewModel.setListeningState(false) 
            android.widget.Toast.makeText(context, context.getString(R.string.search_voice_not_understood), android.widget.Toast.LENGTH_SHORT).show()
            recognizer.destroy()
        }
        override fun onResults(results: android.os.Bundle?) {
            viewModel.setListeningState(false)
            val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { text ->
                viewModel.onVoiceInputReceived(text)
            }
            recognizer.destroy()
        }
        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    })
    recognizer.startListening(intent)
}

@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onAiSearchSubmit: (String) -> Unit,
    onLoadMore: () -> Unit,
    onProductClick: (com.dukkan.domain.model.SearchProduct) -> Unit,
    onCollectionClick: (com.dukkan.domain.model.SearchCollection) -> Unit,
    onOpenFilters: () -> Unit,
    onDismissFilters: () -> Unit,
    onApplyFilters: (com.dukkan.domain.model.SearchFilter) -> Unit,
    onClearFilters: () -> Unit,
    onClarificationAnswerChange: (String) -> Unit,
    onClarificationAnswerSubmit: () -> Unit,
    onRetryAiSearch: () -> Unit,
    onCancelAiSearch: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onMicClick: () -> Unit
) {
    val listState = rememberLazyListState()

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

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(uiState.clarificationQuestion) {
        if (uiState.clarificationQuestion != null && uiState.lastSearchSource == com.dukkan.search.uistate.SearchSource.VOICE) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                kotlinx.coroutines.delay(500)
                onMicClick()
            }
        }
    }

    val showDropdown = uiState.queryInput.isNotBlank() &&
        uiState.submittedQuery != uiState.queryInput &&
        (uiState.predictiveProducts.isNotEmpty() || uiState.predictiveCollections.isNotEmpty())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ChatFab(onClick = onNavigateToChat)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                        ) {
                            SearchBar(
                                query = uiState.queryInput,
                                onQueryChange = onQueryChange,
                                onSearchSubmit = onSearchSubmit,
                                onMicClick = onMicClick,
                                onAiSearchClick = { onAiSearchSubmit(uiState.queryInput) },
                                isAiSearchLoading = uiState.isAiSearchLoading,
                                isListening = uiState.isListening,
                                isMicEnabled = uiState.clarificationSessionId == null,
                                modifier = Modifier.weight(1f)
                            )
                            androidx.compose.material3.IconButton(
                                onClick = onOpenFilters,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (uiState.activeFilters.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.search_filters),
                                    tint = if (uiState.activeFilters.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
                                            label = { Text(stringResource(R.string.search_price_range_label, filters.minPrice ?: 0, filters.maxPrice ?: stringResource(R.string.search_any))) },
                                            trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = stringResource(R.string.search_clear)) },
                                            colors = androidx.compose.material3.InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    }
                                }
                                if (filters.availableOnly) {
                                    item {
                                        androidx.compose.material3.InputChip(
                                            selected = true,
                                            onClick = { onApplyFilters(filters.copy(availableOnly = false)) },
                                            label = { Text(stringResource(R.string.search_in_stock)) },
                                            trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = stringResource(R.string.search_clear)) },
                                            colors = androidx.compose.material3.InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    }
                                }
                                items(filters.vendors) { vendor ->
                                    androidx.compose.material3.InputChip(
                                        selected = true,
                                        onClick = { onApplyFilters(filters.copy(vendors = filters.vendors - vendor)) },
                                        label = { Text(vendor) },
                                        trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = stringResource(R.string.search_clear)) },
                                        colors = androidx.compose.material3.InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                                items(filters.productTypes) { type ->
                                    androidx.compose.material3.InputChip(
                                        selected = true,
                                        onClick = { onApplyFilters(filters.copy(productTypes = filters.productTypes - type)) },
                                        label = { Text(type) },
                                        trailingIcon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = stringResource(R.string.search_clear)) },
                                        colors = androidx.compose.material3.InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
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

                        ClarificationPrompt(
                            question = uiState.clarificationQuestion?.asString(),
                            answer = uiState.clarificationAnswerInput,
                            message = uiState.aiMessage?.asString(),
                            isLoading = uiState.isAiSearchLoading,
                            isError = uiState.isAiError,
                            isListening = uiState.isListening,
                            onAnswerChange = onClarificationAnswerChange,
                            onSubmitAnswer = onClarificationAnswerSubmit,
                            onMicClick = onMicClick,
                            onRetry = onRetryAiSearch,
                            onCancel = onCancelAiSearch
                        )
                    }
                }

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
                                text = uiState.error?.asString() ?: "",
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
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            if (uiState.submittedQuery.isNotBlank()) {
                                item {
                                    Text(
                                        text = stringResource(
                                            R.string.search_results_count,
                                            uiState.totalCount,
                                            uiState.submittedQuery
                                        ),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

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
                                                .size(36.dp)
                                                .padding(4.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isFilterSheetOpen) {
            com.dukkan.search.components.FilterBottomSheet(
                initialFilters = uiState.activeFilters,
                availableVendors = uiState.availableVendors,
                availableTypes = uiState.availableProductTypes,
                onDismissRequest = onDismissFilters,
                onApplyFilters = onApplyFilters,
                onClearFilters = onClearFilters
            )
        }
    }
}
