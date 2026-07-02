package com.dukkan.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.dukkan.search.R
import androidx.compose.ui.unit.dp
import com.msayeh.domain.model.SearchFilter
import com.msayeh.domain.model.SearchProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialFilters: SearchFilter,
    availableVendors: List<String>,
    availableTypes: List<String>,
    onDismissRequest: () -> Unit,
    onApplyFilters: (SearchFilter) -> Unit,
    onClearFilters: () -> Unit
) {
    var minPrice by remember { mutableStateOf(initialFilters.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(initialFilters.maxPrice?.toString() ?: "") }
    var availableOnly by remember { mutableStateOf(initialFilters.availableOnly) }
    var selectedVendors by remember { mutableStateOf(initialFilters.vendors.toSet()) }
    var selectedTypes by remember { mutableStateOf(initialFilters.productTypes.toSet()) }



    val minVal = minPrice.toDoubleOrNull()
    val maxVal = maxPrice.toDoubleOrNull()
    val isPriceError = minVal != null && maxVal != null && minVal > maxVal

    val activeCount = listOf(
        minVal != null,
        maxVal != null,
        availableOnly,
        selectedVendors.isNotEmpty(),
        selectedTypes.isNotEmpty()
    ).count { it }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(stringResource(R.string.search_filters), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Price Range
                Text(stringResource(R.string.search_price_range), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text(stringResource(R.string.search_min)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = isPriceError
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text(stringResource(R.string.search_max)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = isPriceError
                    )
                }
                if (isPriceError) {
                    Text(
                        text = stringResource(R.string.search_min_max_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Availability
                Text(stringResource(R.string.search_availability), style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = availableOnly,
                        onCheckedChange = { availableOnly = it }
                    )
                    Text(stringResource(R.string.search_in_stock_only))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vendor / Brand
                Text(stringResource(R.string.search_brand_vendor), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (availableVendors.isEmpty()) {
                    Text(
                        stringResource(R.string.search_brand_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableVendors) { vendor ->
                            FilterChip(
                                selected = selectedVendors.contains(vendor),
                                onClick = {
                                    selectedVendors = if (selectedVendors.contains(vendor)) {
                                        selectedVendors - vendor
                                    } else {
                                        selectedVendors + vendor
                                    }
                                },
                                label = { Text(vendor) },
                                leadingIcon = if (selectedVendors.contains(vendor)) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Product Type
                Text(stringResource(R.string.search_product_type), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (availableTypes.isEmpty()) {
                    Text(
                        stringResource(R.string.search_type_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableTypes) { type ->
                            FilterChip(
                                selected = selectedTypes.contains(type),
                                onClick = {
                                    selectedTypes = if (selectedTypes.contains(type)) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                                },
                                label = { Text(type) },
                                leadingIcon = if (selectedTypes.contains(type)) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClearFilters) {
                    Text(stringResource(R.string.search_clear_all))
                }
                Button(
                    onClick = {
                        if (!isPriceError) {
                            onApplyFilters(
                                SearchFilter(
                                    minPrice = minVal,
                                    maxPrice = maxVal,
                                    availableOnly = availableOnly,
                                    vendors = selectedVendors.toList(),
                                    productTypes = selectedTypes.toList()
                                )
                            )
                        }
                    },
                    enabled = !isPriceError
                ) {
                    Text(if (activeCount > 0) stringResource(R.string.search_apply_with_count, activeCount) else stringResource(R.string.search_apply))
                }
            }
        }
    }
}
