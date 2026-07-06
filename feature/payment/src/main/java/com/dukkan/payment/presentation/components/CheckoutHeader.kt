package com.dukkan.payment.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.theme.BricolageGrotesque

@Composable
internal fun CheckoutHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontFamily = BricolageGrotesque,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 21.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}