package com.dukkan.product_details.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dukkan.product_details.R

@Composable
fun ProductTopBar(
    isFavorite: Boolean,
    isScrolled: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.background else Color.Transparent,
        label = "TopBarBackground"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CircleIconButton(
            contentDescription = stringResource(R.string.product_details_back),
            onClick = onBackClick,
            isScrolled = isScrolled,
        ) { tint -> drawBackChevron(tint) }
        CircleIconButton(
            contentDescription = stringResource(R.string.product_details_favorite),
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            onClick = onFavoriteClick,
            isScrolled = isScrolled,
        ) { tint -> drawHeart(tint, filled = isFavorite) }
    }
}

@Composable
private fun CircleIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    isScrolled: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    icon: DrawScope.(tint: Color) -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = if (isScrolled) Color.Transparent else MaterialTheme.colorScheme.surface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(22.dp)
                    .semantics { this.contentDescription = contentDescription },
            ) {
                icon(tint)
            }
        }
    }
}

private fun DrawScope.drawBackChevron(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.6f, h * 0.22f)
        lineTo(w * 0.34f, h * 0.5f)
        lineTo(w * 0.6f, h * 0.78f)
    }
    drawPath(
        path = path,
        color = tint,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private fun DrawScope.drawHeart(tint: Color, filled: Boolean) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.84f)
        cubicTo(w * 0.12f, h * 0.56f, w * 0.06f, h * 0.30f, w * 0.27f, h * 0.21f)
        cubicTo(w * 0.40f, h * 0.16f, w * 0.50f, h * 0.27f, w * 0.5f, h * 0.33f)
        cubicTo(w * 0.50f, h * 0.27f, w * 0.60f, h * 0.16f, w * 0.73f, h * 0.21f)
        cubicTo(w * 0.94f, h * 0.30f, w * 0.88f, h * 0.56f, w * 0.5f, h * 0.84f)
        close()
    }
    if (filled) {
        drawPath(path = path, color = tint)
    } else {
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
