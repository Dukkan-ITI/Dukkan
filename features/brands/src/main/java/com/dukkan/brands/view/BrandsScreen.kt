package com.dukkan.brands.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.brands.R
import com.dukkan.brands.uiState.BrandsUiState
import com.dukkan.brands.viewModel.BrandsViewModel
import com.dukkan.design_system.components.ErrorScreen
import com.dukkan.design_system.components.FilterChip
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.Brand
import com.dukkan.design_system.R as DesignSystemR

@Composable
fun BrandsScreenContent(
    brands: List<Brand>,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onBrandClick: (Brand) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.brands),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(DesignSystemR.string.viewing_cached_data),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = bottomBarSpace()),
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
    val uiState by viewModel.brandsState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is BrandsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is BrandsUiState.Error -> {
                if (!isOnline) {
                    ErrorScreen(
                        message = stringResource(DesignSystemR.string.offline_message),
                        title = stringResource(DesignSystemR.string.offline_title),
                        lottieRawRes = DesignSystemR.raw.no_internet
                    )
                } else {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is BrandsUiState.Success -> {
                if (state.brands.isEmpty()) {
                    if (!isOnline) {
                        ErrorScreen(
                            message = stringResource(DesignSystemR.string.offline_message),
                            title = stringResource(DesignSystemR.string.offline_title),
                            lottieRawRes = DesignSystemR.raw.no_internet
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(id = R.string.no_brands_found))
                        }
                    }
                } else {
                    BrandsScreenContent(
                        brands = state.brands,
                        isOnline = isOnline,
                        onBackClick = onBackClick,
                        onBrandClick = onBrandClick
                    )
                }
            }
        }
    }
}
