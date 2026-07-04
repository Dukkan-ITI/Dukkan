package com.dukkan.brands.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.design_system.components.FilterChip
import com.dukkan.domain.model.Brand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandsScreenContent(
    brands: List<Brand>,
    onBackClick: () -> Unit,
    onBrandClick: (Brand) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.brands)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(brands) { brand ->
                FilterChip(
                    label = brand.name,
                    isSelected = false,
                    onClick = { onBrandClick(brand) }
                )
            }
        }
    }
}

@Composable
fun BrandsScreen(
    onBackClick: () -> Unit,
    onBrandClick: (Brand) -> Unit,
    viewModel: BrandsViewModel = hiltViewModel()
) {
    val brands by viewModel.brandsState.collectAsState()

    BrandsScreenContent(
        brands = brands,
        onBackClick = onBackClick,
        onBrandClick = onBrandClick
    )
}