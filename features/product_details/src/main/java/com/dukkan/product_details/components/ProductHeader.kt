package com.dukkan.product_details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.theme.AppTheme
import com.dukkan.domain.model.Money


/**
 * Title block of the product details screen: an optional category label, the
 * product [title] and its [price].
 */
@Composable
fun ProductHeader(
    title: String,
    price: Money,
    modifier: Modifier = Modifier,
    category: String? = null,
    rating: Float? = null,
    reviewCount: Int? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!category.isNullOrBlank()) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (rating != null) {
                com.dukkan.design_system.components.StarRatingBar(
                    rating = rating,
                    reviewCount = reviewCount,
                    starSize = 16.dp,
                )
            }
        }
        Text(
            text = price.asString(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductHeaderPreview() {
    AppTheme {
        ProductHeader(
            title = "Boxy Crop Tee",
            price = Money(amount = 36.toBigDecimal(), currencyCode = "USD"),
            category = "Juno Label",
            modifier = Modifier.padding(24.dp),
        )
    }
}
