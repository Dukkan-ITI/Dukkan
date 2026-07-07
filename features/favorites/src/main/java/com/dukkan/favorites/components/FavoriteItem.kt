package com.dukkan.favorites.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dukkan.design_system.components.ProductCard
import com.dukkan.domain.model.FavoriteProduct


@Composable
fun FavoriteItem(
    product: FavoriteProduct,
    onUnfav: () -> Unit,
    onProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProductCard(
        title = product.title,
        priceLabel = product.toMoney().asString(),
        imageUrl = product.imageUrl,
        isFavorite = true,
        rating = product.rating,
        reviewCount = product.reviewCount,
        onFavoriteClick = onUnfav,
        onCardClick = onProductClick,
        modifier = modifier
    )
}
