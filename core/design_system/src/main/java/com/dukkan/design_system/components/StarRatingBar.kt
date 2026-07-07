package com.dukkan.design_system.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor

/**
 * Read-only star rating bar. Renders filled, half, and empty stars based on [rating].
 * Optionally shows the review count in parentheses.
 */
@Composable
fun StarRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    reviewCount: Int? = null,
    starSize: Dp = 14.dp,
    starColor: Color = Color(0xFFFFC107),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        val fullStars = floor(rating).toInt()
        val hasHalf = (rating - fullStars) >= 0.5f
        val emptyStars = maxStars - fullStars - if (hasHalf) 1 else 0

        repeat(fullStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize),
            )
        }
        if (hasHalf) {
            Icon(
                imageVector = Icons.Default.StarHalf,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize),
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize),
            )
        }

        if (reviewCount != null) {
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
