package com.msayeh.product_details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.design_system.theme.AppTheme
import com.msayeh.product_details.R

/**
 * "Details" heading followed by the product [description].
 */
@Composable
fun ProductDetailsSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.product_details_details),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.inverseOnSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailsSectionPreview() {
    AppTheme {
        ProductDetailsSection(
            description = "Cut from soft mid-weight cotton with a relaxed, boxy fit and a " +
                    "cropped hem. Pairs effortlessly with high-waisted denim or joggers.",
            modifier = Modifier.padding(24.dp),
        )
    }
}
