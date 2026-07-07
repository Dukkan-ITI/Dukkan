package com.dukkan.product_details.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.res.stringResource
import com.dukkan.product_details.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.design_system.components.ErrorScreen
import com.dukkan.design_system.components.LoadingScreen
import com.dukkan.domain.model.Product
import com.dukkan.product_details.components.AddToCartBar
import com.dukkan.product_details.components.ProductDetailsSection
import com.dukkan.product_details.components.ProductHeader
import com.dukkan.product_details.components.ProductImagePager
import com.dukkan.product_details.components.ReviewsSection
import com.dukkan.product_details.components.VariantSelector
import com.dukkan.product_details.components.WriteReviewBottomSheet
import com.dukkan.product_details.viewmodel.ProductDetailsState
import com.dukkan.product_details.viewmodel.ProductDetailsViewModel

@Composable
fun ProductDetailsScreen(
    onBackClick: () -> Unit = {},
    onNavigateToGuestPlaceholder: (String) -> Unit = {},
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state.showGuestDialog) {
        if (state.showGuestDialog) {
            onNavigateToGuestPlaceholder("Product")
            viewModel.dismissGuestDialog()
        }
    }

    ProductDetailsContent(
        state = state,
        onBackClick = onBackClick,
        onRefresh = { viewModel.getProductDetails() },
        onFavoriteClick = viewModel::toggleFavorite,
        onAddToCartClick = viewModel::addToCart,
        onWriteReviewClick = viewModel::openReviewSheet,
        onDismissReviewSheet = viewModel::dismissReviewSheet,
        onSubmitReview = viewModel::submitReview,
    )
}

@Composable
private fun ProductDetailsContent(
    state: ProductDetailsState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteClick: (Product, Boolean) -> Unit,
    onAddToCartClick: (String) -> Unit,
    onWriteReviewClick: () -> Unit,
    onDismissReviewSheet: () -> Unit,
    onSubmitReview: (Int, String, String) -> Unit,
) {
    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> ErrorScreen(message = state.error, onRetry = onRefresh)
        state.product != null -> LoadedProductDetails(
            product = state.product,
            state = state,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick,
            onAddToCartClick = onAddToCartClick,
            onWriteReviewClick = onWriteReviewClick,
            onDismissReviewSheet = onDismissReviewSheet,
            onSubmitReview = onSubmitReview,
        )
    }
}

@Composable
private fun LoadedProductDetails(
    product: Product,
    state: ProductDetailsState,
    onBackClick: () -> Unit,
    onFavoriteClick: (Product, Boolean) -> Unit,
    onAddToCartClick: (String) -> Unit,
    onWriteReviewClick: () -> Unit,
    onDismissReviewSheet: () -> Unit,
    onSubmitReview: (Int, String, String) -> Unit,
) {
    val images = product.images?.takeIf { it.isNotEmpty() } ?: listOf(product.featuredImage)
    val variants = product.variants.orEmpty()

    var selectedVariant by remember(product.id) { mutableStateOf(variants.firstOrNull()) }
    val isFavorite = state.favoriteIds.contains(product.id)

    Box(modifier = Modifier.fillMaxSize()) {
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
                    images = images.mapNotNull { it },
                    isFavorite = isFavorite,
                    onBackClick = onBackClick,
                    onFavoriteClick = { onFavoriteClick(product, isFavorite) },
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ProductHeader(
                        title = product.title,
                        price = selectedVariant?.price ?: product.maxPrice,
                        category = product.productType,
                        rating = product.averageRating,
                        reviewCount = product.reviews.size.takeIf { it > 0 }
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

                    // Reviews section
                    ReviewsSection(
                        reviews = product.reviews,
                        averageRating = product.averageRating,
                        isLoggedIn = state.isLoggedIn,
                        onWriteReviewClick = onWriteReviewClick,
                    )
                }
            }

            AddToCartBar(
                price = selectedVariant?.price ?: product.maxPrice,
                onAddToCart = {
                    val variantId = selectedVariant?.id ?: product.variants?.firstOrNull()?.id ?: ""
                    if (variantId.isNotEmpty()) {
                        onAddToCartClick(variantId)
                    }
                },
                isLoading = state.isAddingToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding(),
            )
        }

        // Cart added toast
        AnimatedVisibility(
            visible = state.cartAddedSuccess,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.product_details_success),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.product_details_added_to_cart),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Review success toast
        AnimatedVisibility(
            visible = state.reviewSuccess,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Review submitted!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }


    // Write review bottom sheet
    if (state.showReviewSheet) {
        WriteReviewBottomSheet(
            isSubmitting = state.isSubmittingReview,
            errorMessage = state.reviewError,
            onDismiss = onDismissReviewSheet,
            onSubmit = onSubmitReview,
        )
    }
}