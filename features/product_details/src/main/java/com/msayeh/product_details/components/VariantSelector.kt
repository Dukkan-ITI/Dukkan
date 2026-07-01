package com.msayeh.product_details.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.design_system.theme.AppTheme
import com.msayeh.domain.model.Money
import com.msayeh.domain.model.ProductVariant
import com.msayeh.product_details.R

/**
 * Renders the product's [variants] as a row of selectable size chips. Variants
 * that are not available for sale are shown disabled.
 */
@Composable
fun VariantSelector(
    variants: List<ProductVariant>,
    selectedVariant: ProductVariant?,
    onVariantSelected: (ProductVariant) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.product_details_select_size),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            variants.forEach { variant ->
                SizeChip(
                    label = variant.title,
                    selected = variant.id == selectedVariant?.id,
                    enabled = variant.availableForSale,
                    onClick = { onVariantSelected(variant) },
                )
            }
        }
    }
}

@Composable
private fun SizeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        enabled -> MaterialTheme.colorScheme.onBackground
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minWidth = 64.dp, minHeight = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VariantSelectorPreview() {
    val variants = listOf("XS", "S", "M", "L", "XL").mapIndexed { index, size ->
        ProductVariant(
            id = "variant-$index",
            title = size,
            price = Money(amount = 36.toBigDecimal(), currencyCode = "USD"),
            image = null,
            availableForSale = size != "L",
            quantityAvailable = 5,
            compareAtPrice = TODO(),
            selectedOptions = TODO(),
            product = TODO(),
        )
    }
    AppTheme {
        VariantSelector(
            variants = variants,
            selectedVariant = variants.first(),
            onVariantSelected = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
