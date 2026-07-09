package com.dukkan.brands.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.dukkan.brands.uiState.BrandProductsUiState
import com.dukkan.brands.viewModel.BrandProductsEvent
import com.dukkan.brands.viewModel.BrandProductsViewModel
import com.dukkan.design_system.components.ErrorScreen
import com.dukkan.design_system.components.ProductCard
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.Product
import com.dukkan.design_system.R as DesignSystemR

@Composable
fun BrandProductsScreen(
    vendor: String,
    viewModel: BrandProductsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val showGuestDialog by viewModel.showGuestAuthDialog.collectAsStateWithLifecycle()

    if (showGuestDialog) {
        com.dukkan.design_system.components.GuestAuthDialog(
            onDismiss = { viewModel.dismissGuestAuthDialog() },
            onSignInClick = onSignInClick
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->

        }
    }

    LaunchedEffect(vendor) {
        viewModel.fetchProductsByVendor(vendor)
    }

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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = vendor,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is BrandProductsUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                is BrandProductsUiState.Error -> {
                    if (!isOnline) {
                        ErrorScreen(
                            message = stringResource(DesignSystemR.string.offline_message),
                            title = stringResource(DesignSystemR.string.offline_title),
                            lottieRawRes = DesignSystemR.raw.no_internet
                        )
                    } else {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is BrandProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        if (!isOnline) {
                            ErrorScreen(
                                message = stringResource(DesignSystemR.string.offline_message),
                                title = stringResource(DesignSystemR.string.offline_title),
                                lottieRawRes = DesignSystemR.raw.no_internet
                            )
                        } else {
                            Text(text = stringResource(id = R.string.no_products_found))
                        }
                    } else {
                        Column {
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
                            BrandProductsGrid(
                                products = state.products,
                                favoriteIds = state.favoriteIds,
                                onProductClick = onProductClick,
                                onFavoriteClick = { product, isFav ->
                                    viewModel.onFavoriteClick(product, isFav)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandProductsGrid(
    products: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onFavoriteClick: (Product, Boolean) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomBarSpace()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            val isFav = product.id in favoriteIds
            ProductCard(
                title = product.title,
                priceLabel = product.maxPrice.asString(),
                imageUrl = product.featuredImage?.url,
                isFavorite = isFav,
                onFavoriteClick = {
                    onFavoriteClick(product, isFav)
                },
                onCardClick = {
                    onProductClick(product)
                },
                modifier = Modifier.padding(bottom = 18.dp)
            )
        }
    }
}