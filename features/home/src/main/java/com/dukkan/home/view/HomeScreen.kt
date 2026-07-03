package com.dukkan.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.Category.Category
import com.dukkan.home.components.HomeBanner
import com.dukkan.home.components.HomeCategoriesSection
import com.dukkan.home.components.HomeHeader
import com.dukkan.home.components.HomeSearchBar
import com.dukkan.home.components.homeProductSection
import com.dukkan.home.uiState.HomeUiState
import com.dukkan.home.viewmodel.HomeViewModel
import com.dukkan.domain.model.Product

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProductDetails: (productId: String) -> Unit = {},
    onSeeAllClicked: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        modifier = modifier,
        uiState = uiState,
        onSeeAllClicked = onSeeAllClicked,
        onSearchClick = onSearchClick,
        onFavoriteClick = { product, isFavorite ->
            viewModel.onFavoriteClick(product, isFavorite)
        },
        onProductClick = { product ->
            onNavigateToProductDetails(product.id)
        },
        onNavigateToCategories = onNavigateToCategories,
        onCategoryClick = onCategoryClick,
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onSeeAllClicked: () -> Unit,
    onSearchClick: () -> Unit = {},
    onFavoriteClick: (product: Product, isFavorite: Boolean) -> Unit,
    onProductClick: (product: Product) -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = bottomBarSpace())
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                is HomeUiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                is HomeUiState.Success -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeHeader()
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeSearchBar(onSearchClick = onSearchClick)
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeBanner()
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeCategoriesSection(
                            categories = uiState.categories,
                            onSeeAllClick = onNavigateToCategories,
                            onCategoryClick = onCategoryClick as (String) -> Unit
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    homeProductSection(
                        products = uiState.products,
                        favoriteIds = uiState.favoriteIds,
                        onSeeAllClick = onSeeAllClicked,
                        onFavoriteClick = onFavoriteClick,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}