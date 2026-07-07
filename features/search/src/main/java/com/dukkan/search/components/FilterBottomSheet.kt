package com.dukkan.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.search.R
import com.dukkan.domain.model.SearchFilter

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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    stringResource(R.string.search_filters),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Price Range
                FilterSectionHeader(stringResource(R.string.search_price_range))
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
                        isError = isPriceError,
                        colors = colorfulTextFieldColors()
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text(stringResource(R.string.search_max)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = isPriceError,
                        colors = colorfulTextFieldColors()
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
                FilterSectionHeader(stringResource(R.string.search_availability))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (availableOnly) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                        .border(1.dp, if (availableOnly) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Checkbox(
                        checked = availableOnly,
                        onCheckedChange = { availableOnly = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.search_in_stock_only),
                        fontWeight = if (availableOnly) FontWeight.Bold else FontWeight.Normal,
                        color = if (availableOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Vendor / Brand
                FilterSectionHeader(stringResource(R.string.search_brand_vendor))
                if (availableVendors.isEmpty()) {
                    EmptyFilterText(stringResource(R.string.search_brand_empty))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableVendors) { vendor ->
                            ColorfulFilterChip(
                                label = vendor,
                                selected = selectedVendors.contains(vendor),
                                onClick = {
                                    selectedVendors = if (selectedVendors.contains(vendor)) {
                                        selectedVendors - vendor
                                    } else {
                                        selectedVendors + vendor
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Product Type
                FilterSectionHeader(stringResource(R.string.search_product_type))
                if (availableTypes.isEmpty()) {
                    EmptyFilterText(stringResource(R.string.search_type_empty))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableTypes) { type ->
                            ColorfulFilterChip(
                                label = type,
                                selected = selectedTypes.contains(type),
                                onClick = {
                                    selectedTypes = if (selectedTypes.contains(type)) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                                }
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
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(stringResource(R.string.search_clear_all), fontWeight = FontWeight.SemiBold)
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
                    enabled = !isPriceError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        if (activeCount > 0) stringResource(R.string.search_apply_with_count, activeCount) else stringResource(R.string.search_apply),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun EmptyFilterText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun colorfulTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorfulFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.5.dp
        ),
        shape = RoundedCornerShape(100.dp)
    )
}
