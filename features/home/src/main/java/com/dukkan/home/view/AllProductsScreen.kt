package com.dukkan.home.view

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.design_system.components.ProductCard
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.asString
import com.dukkan.home.R
import com.dukkan.home.uistate.AllProductsUiState
import com.dukkan.home.viewmodel.AllProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    viewModel: AllProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.all_products),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AllProductsUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is AllProductsUiState.Error -> {
                    Text(text = state.message.asString(), color = MaterialTheme.colorScheme.error)
                }

                is AllProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text(text = stringResource(R.string.no_products_found))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.products) { product ->
                                val isFav = product.id in state.favoriteIds
                                ProductCard(
                                    title = product.title,
                                    priceLabel = product.maxPrice.asString(),
                                    imageUrl = product.featuredImage?.url,
                                    isFavorite = isFav,
                                    onFavoriteClick = {
                                        viewModel.onFavoriteClick(product, isFav)
                                    },
                                    onCardClick = {
                                        onProductClick(product)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
