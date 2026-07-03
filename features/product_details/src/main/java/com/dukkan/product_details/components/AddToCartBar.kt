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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.components.PrimaryButton
import com.dukkan.design_system.theme.BricolageGrotesque
import com.dukkan.design_system.theme.HankenGrotesque
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.asString
import com.dukkan.product_details.R

@Composable
fun AddToCartBar(price: Money, onAddToCart: () -> Unit, modifier: Modifier = Modifier, isLoading: Boolean = false) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier
            .drawWithContent {
                val widthPx = 1.dp.toPx()
                drawContent()
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, widthPx / 2),
                    end = Offset(size.width, widthPx / 2),
                    strokeWidth = widthPx
                )
            }
            .padding(24.dp)
            ,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PriceDisplay(price = price)
        AddToCartButton(onClick = onAddToCart, isLoading = isLoading, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PriceDisplay(price: Money) {
    Column {
        Text(
            text = stringResource(R.string.product_details_total),
            fontSize = 11.sp,
            fontWeight = FontWeight.W400,
            fontFamily = HankenGrotesque,
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
        Text(
            text = price.asString(),
            fontSize = 21.sp,
            fontWeight = FontWeight.W800,
            fontFamily = BricolageGrotesque,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AddToCartButton(onClick: () -> Unit, modifier: Modifier = Modifier, isLoading: Boolean = false) {
    PrimaryButton(
        text = stringResource(R.string.product_details_add_to_cart),
        onClick = onClick,
        isLoading = isLoading,
        modifier = modifier
    )
}

@Preview
@Composable
private fun AddToCartBarPreview() {
    AddToCartBar(
        price = Money(amount = 19.99.toBigDecimal(), currencyCode = "USD"),
        onAddToCart = {}
    )
}