package com.msayeh.product_details.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.design_system.components.ErrorScreen
import com.example.design_system.components.LoadingScreen
import com.msayeh.domain.model.Product
import com.msayeh.product_details.components.AddToCartBar
import com.msayeh.product_details.components.ProductDetailsSection
import com.msayeh.product_details.components.ProductHeader
import com.msayeh.product_details.components.ProductImagePager
import com.msayeh.product_details.components.VariantSelector
import com.msayeh.product_details.viewmodel.ProductDetailsState
import com.msayeh.product_details.viewmodel.ProductDetailsViewModel

@Composable
fun ProductDetailsScreen(
    onBackClick: () -> Unit = {},
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getProductDetails()
    }

    ProductDetailsContent(
        state = state,
        onBackClick = onBackClick,
        onRefresh = { viewModel.getProductDetails() },
    )
}

@Composable
private fun ProductDetailsContent(
    state: ProductDetailsState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> ErrorScreen(message = state.error, onRetry = onRefresh)
        state.product != null -> LoadedProductDetails(
            product = state.product,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun LoadedProductDetails(
    product: Product,
    onBackClick: () -> Unit,
) {
    val images = product.images?.takeIf { it.isNotEmpty() } ?: listOf(product.featuredImage)
    val variants = product.variants.orEmpty()

    var selectedVariant by remember(product.id) { mutableStateOf(variants.firstOrNull()) }
    var isFavorite by remember(product.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ProductImagePager(
                images = images,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onFavoriteClick = { isFavorite = !isFavorite },
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ProductHeader(
                    title = product.title,
                    price = selectedVariant?.price ?: product.maxPrice,
                    category = product.productType,
                )

                if (variants.size > 1) {
                    VariantSelector(
                        variants = variants,
                        selectedVariant = selectedVariant,
                        onVariantSelected = { selectedVariant = it },
                    )
                }

                product.description?.takeIf { it.isNotBlank() }?.let { description ->
                    ProductDetailsSection(description = description)
                }
            }
        }

        AddToCartBar(
            price = selectedVariant?.price ?: product.maxPrice,
            onAddToCart = { /* Handle add to cart */ },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding(),
        )
    }
}
