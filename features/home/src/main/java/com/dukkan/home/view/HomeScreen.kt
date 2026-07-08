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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.ads.components.CouponBannerSection
import com.dukkan.design_system.components.ChatFab
import com.dukkan.design_system.components.FloatingBottomBarHeight
import com.dukkan.design_system.components.FloatingBottomBarMargin
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.Brand
import com.dukkan.home.components.HomeBrandsSection
import com.dukkan.home.components.HomeCategoriesSection
import com.dukkan.home.components.HomeHeader
import com.dukkan.home.components.homeProductSection
import com.dukkan.home.uistate.HomeUiState
import com.dukkan.home.viewmodel.HomeEvent
import com.dukkan.home.viewmodel.HomeViewModel
import com.dukkan.domain.model.Product

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProductDetails: (productId: String) -> Unit = {},
    onSeeAllClicked: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onCategoryClick: (com.dukkan.domain.model.Category.Category) -> Unit = {},
    onNavigateToBrands: () -> Unit = {},
    onBrandClick: (Brand) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onSignInClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val firstName by viewModel.firstName.collectAsStateWithLifecycle()
    val showGuestDialog by viewModel.showGuestAuthDialog.collectAsStateWithLifecycle()

    if (showGuestDialog) {
        com.dukkan.design_system.components.GuestAuthDialog(
            onDismiss = { viewModel.dismissGuestAuthDialog() },
            onSignInClick = onSignInClick
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            // Handle other events here if any
        }
    }

    HomeScreenContent(
        modifier = modifier,
        uiState = uiState,
        firstName = firstName,
        onSeeAllClicked = onSeeAllClicked,
        onFavoriteClick = { product, isFavorite ->
            viewModel.onFavoriteClick(product, isFavorite)
        },
        onProductClick = { product ->
            onNavigateToProductDetails(product.id)
        },
        onNavigateToCategories = onNavigateToCategories,
        onCategoryClick = onCategoryClick,
        onNavigateToBrands = onNavigateToBrands,
        onBrandClick = onBrandClick,
        onNavigateToChat = onNavigateToChat
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    firstName: String? = null,
    onSeeAllClicked: () -> Unit,
    onFavoriteClick: (product: Product, isFavorite: Boolean) -> Unit,
    onProductClick: (product: Product) -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onCategoryClick: (com.dukkan.domain.model.Category.Category) -> Unit = {},
    onNavigateToBrands: () -> Unit = {},
    onBrandClick: (Brand) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ChatFab(onClick = onNavigateToChat,
                modifier = Modifier.padding(bottom = bottomBarSpace()))
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = FloatingBottomBarHeight)
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
                        HomeHeader(firstName = firstName)
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CouponBannerSection(onShopClick = {})
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeCategoriesSection(
                            categories = uiState.categories,
                            onSeeAllClick = onNavigateToCategories,
                            onCategoryClick = onCategoryClick
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeBrandsSection(
                            brands = uiState.brands,
                            onSeeAllClick = onNavigateToBrands,
                            onBrandClick = onBrandClick
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
