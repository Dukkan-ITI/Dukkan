package com.dukkan.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.categories.R
import com.dukkan.categories.uistate.CategoryProductsUiState
import com.dukkan.categories.viewmodel.CategoryProductsEvent
import com.dukkan.categories.viewmodel.CategoryProductsViewModel
import com.dukkan.design_system.components.ProductCard
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.Product

@Composable
fun CategoryProductsScreen(
    categoryId: String,
    categoryName: String,
    viewModel: CategoryProductsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CategoryProductsEvent.NavigateToFavoritesGuest -> onNavigateToFavorites()
            }
        }
    }

    LaunchedEffect(categoryId) {
        viewModel.fetchProductsById(categoryId)
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
                text = categoryName.replaceFirstChar { it.uppercase() },
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
                is CategoryProductsUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                is CategoryProductsUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }

                is CategoryProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text(text = stringResource(id = R.string.no_products_found))
                    } else {
                        CategoryProductsGrid(
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

@Composable
fun CategoryProductsGrid(
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
