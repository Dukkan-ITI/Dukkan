package com.dukkan.categories.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.categories.uiState.CategoryProductsUiState
import com.dukkan.categories.viewmodel.CategoryProductsViewModel
import com.dukkan.design_system.components.ProductCard
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.asString
import androidx.compose.ui.res.stringResource
import com.dukkan.categories.R

@Composable
fun CategoryProductsScreen(
    categoryHandle: String,
    viewModel: CategoryProductsViewModel = hiltViewModel(),
    onProductClick: (Product) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryHandle) {
        viewModel.fetchProductsByHandle(categoryHandle)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is CategoryProductsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is CategoryProductsUiState.Error -> {
                Text(text = state.message)
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
        contentPadding = PaddingValues(16.dp),
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
