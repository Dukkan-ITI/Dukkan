package com.dukkan.design_system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.R
import com.dukkan.design_system.theme.AppThemeDefaults

data class OrderUi(
    val id: String,
    val date: String,
    val itemCount: Int,
    val total: String,
    val status: OrderStatus,
    val isPaid: Boolean,
    val items: List<OrderItemUi>
)

enum class OrderStatus {
    DELIVERED,
    IN_TRANSIT,
    PROCESSING,
    CANCELLED,
    PENDING
}

@Composable
fun OrderCard(order: OrderUi, modifier: Modifier = Modifier, expandable: Boolean = false) {
    var isExpanded by remember { mutableStateOf(false) }
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isExpanded) {
        primaryColor.copy(alpha = 0.07f)
    } else {
        surfaceColor
    }

    val border = if (isExpanded) {
        null
    } else {
        BorderStroke(1.dp, outlineColor.copy(alpha = 0.6f))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (expandable) Modifier.clickable { isExpanded = !isExpanded } else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = border,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "${order.date} · ${
                            stringResource(
                                R.string.profile_order_items,
                                order.itemCount
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.isPaid) {
                            Text(
                                text = stringResource(R.string.profile_order_paid),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = order.total,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        text = when (order.status) {
                            OrderStatus.DELIVERED -> stringResource(R.string.profile_order_status_delivered)
                            OrderStatus.IN_TRANSIT -> stringResource(R.string.profile_order_status_in_transit)
                            OrderStatus.PROCESSING -> stringResource(R.string.profile_order_status_processing)
                            OrderStatus.CANCELLED -> stringResource(R.string.profile_order_status_cancelled)
                            OrderStatus.PENDING -> stringResource(R.string.profile_order_status_pending)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> MaterialTheme.colorScheme.primary
                            OrderStatus.IN_TRANSIT -> AppThemeDefaults.extendedColors.orderStatusInTransit
                            OrderStatus.PROCESSING -> AppThemeDefaults.extendedColors.orderStatusProcessing
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            OrderStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            if (expandable) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        order.items.forEach { item ->
                            OrderLineItemRow(item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}