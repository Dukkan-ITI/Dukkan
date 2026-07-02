package com.dukkan.shopping_cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dukkan.shopping_cart.R
import com.msayeh.domain.model.cart.CartLine

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemRow(
    item: CartLine,
    onQuantityChanged: (CartLine, Int) -> Unit,
    onRemoveClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onRemoveClick()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.targetValue != SwipeToDismissBoxValue.Settled || dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        content = {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .heightIn(min = 130.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.merchandise.image?.url,
            contentDescription = item.merchandise.image?.altText
                ?: stringResource(R.string.product_image_content_desc),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_placeholder),
            error = painterResource(R.drawable.ic_placeholder),
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.merchandise.product.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.price_format,
                            item.cost.amountPerQuantity.amount.toPlainString(),
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onRemoveClick() }
                    )
                    if (item.merchandise.title.isNotBlank() && item.merchandise.title.lowercase() != "default title") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.merchandise.title,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quantity_decrease),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .clickable {
                                if (item.quantity > 1) {
                                    onQuantityChanged(item, item.quantity - 1)
                                } else {
                                    onRemoveClick()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "${item.quantity}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.quantity_increase),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .clickable { onQuantityChanged(item, item.quantity + 1) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
    }
    )
}