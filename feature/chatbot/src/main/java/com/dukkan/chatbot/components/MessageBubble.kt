package com.dukkan.chatbot.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.components.OrderCard
import com.dukkan.design_system.components.OrderItemUi
import com.dukkan.design_system.components.OrderStatus
import com.dukkan.design_system.components.OrderUi
import com.dukkan.design_system.components.ProductCard
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.chatbot.ChatMessage
import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.model.orders.OrderDisplayStatus

@Composable
fun MessageBubble(
    message: ChatMessage,
    onProductClick: (String) -> Unit = {},
    onFavoriteClick: (SearchProduct) -> Unit = {}
) {
    val isUser = message.isFromUser
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (message.text.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (message.products.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 32.dp)
            ) {
                items(message.products) { product ->
                    ProductCard(
                        title = product.title,
                        priceLabel = product.price.asString(),
                        imageUrl = product.imageUrl,
                        isFavorite = false,
                        onFavoriteClick = { onFavoriteClick(product) },
                        onCardClick = { onProductClick(product.id) },
                        modifier = Modifier.width(160.dp),
                        rating = product.averageRating,
                        reviewCount = product.reviewCount
                    )
                }
            }
        }

        if (message.orders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                message.orders.forEach { order ->
                    OrderCard(
                        order = order.toUiModel(),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        }
    }
}

private fun Order.toUiModel(): OrderUi {
    return OrderUi(
        id = id,
        date = processedAt,
        itemCount = lineItems.sumOf { it.quantity },
        total = totalPrice.asString(),
        status = when (displayStatus) {
            OrderDisplayStatus.DELIVERED -> OrderStatus.DELIVERED
            OrderDisplayStatus.IN_TRANSIT -> OrderStatus.IN_TRANSIT
            OrderDisplayStatus.PROCESSING -> OrderStatus.PROCESSING
            OrderDisplayStatus.CANCELLED -> OrderStatus.CANCELLED
            OrderDisplayStatus.PENDING -> OrderStatus.PENDING
        },
        isPaid = financialStatus == "PAID",
        items = lineItems.map { item ->
            OrderItemUi(
                title = item.title,
                quantity = item.quantity,
                totalPrice = item.totalPrice.asString(),
                imageUrl = item.imageUrl
            )
        }
    )
}

@Composable
fun BotAvatar() {
    val infiniteTransition = rememberInfiniteTransition(label = "BotAvatarTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BotAvatarRotation"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val aiGradient = listOf(primary, tertiary, secondary, primary)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .drawBehind {
                rotate(angle) {
                    drawCircle(
                        brush = Brush.sweepGradient(aiGradient, center = Offset(size.width/2f, size.height/2f)),
                        radius = size.width / 2f,
                        center = Offset(size.width/2f, size.height/2f)
                    )
                }
            }
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val textGradient = Brush.linearGradient(
            colors = listOf(primary, tertiary)
        )
        Text(
            text = "D",
            style = TextStyle(
                brush = textGradient,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black
            )
        )
    }
}
