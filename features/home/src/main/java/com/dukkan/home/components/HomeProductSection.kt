package com.dukkan.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.dukkan.home.R
import com.example.design_system.theme.AppTheme
import com.msayeh.domain.model.Product
import com.msayeh.domain.model.asString

fun LazyGridScope.homeProductSection(
    products: List<Product> = emptyList(),
    onSeeAllClick: () -> Unit = {},
    onFavoriteClick: (Product, Boolean) -> Unit = { _, _ -> },
    onProductClick: (Product) -> Unit = { _ -> }
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.picked_for_you),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.see_all),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }

    items(products) { product ->
        HomeProductCard(
            title = product.title,
            priceLabel = product.maxPrice.asString(),
            imageUrl = product.featuredImage?.url,
            isFavorite = false,
            onFavoriteClick = {
                onFavoriteClick(product, false)
            },
            onCardClick = {
                onProductClick(product)
            },
            modifier = Modifier.padding(bottom = 18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeProductSectionPreview() {
    AppTheme {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            homeProductSection()
        }
    }
}
