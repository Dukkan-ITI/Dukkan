package com.dukkan.order_list.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.theme.AppThemeDefaults
import com.dukkan.order_list.R
import com.dukkan.domain.model.orders.OrderDisplayStatus

@Composable
fun OrderStatusBadge(
    status: OrderDisplayStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        OrderDisplayStatus.DELIVERED -> MaterialTheme.colorScheme.primary
        OrderDisplayStatus.IN_TRANSIT -> AppThemeDefaults.extendedColors.orderStatusInTransit
        OrderDisplayStatus.PROCESSING -> AppThemeDefaults.extendedColors.orderStatusProcessing
        OrderDisplayStatus.CANCELLED -> MaterialTheme.colorScheme.error
        OrderDisplayStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val text = when (status) {
        OrderDisplayStatus.DELIVERED  -> stringResource(R.string.order_status_delivered)
        OrderDisplayStatus.IN_TRANSIT -> stringResource(R.string.order_status_in_transit)
        OrderDisplayStatus.PROCESSING -> stringResource(R.string.order_status_processing)
        OrderDisplayStatus.CANCELLED  -> stringResource(R.string.order_status_cancelled)
        OrderDisplayStatus.PENDING    -> stringResource(R.string.order_status_pending)
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(vertical = 4.dp)
    )
}


