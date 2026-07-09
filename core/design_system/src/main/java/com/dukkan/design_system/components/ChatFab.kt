package com.dukkan.design_system.components

import androidx.compose.animation.core.Animatable
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChatFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AiBotTransition")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AiBotRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiBotScale"
    )

    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiBotFloat"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val aiGradient = listOf(
        primary,
        secondary,
        tertiary,
        secondary,
        primary
    )

    val fabOffsetState = remember { mutableStateOf(Offset.Zero) }

    val letterAbsOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(Unit) {
        snapshotFlow { fabOffsetState.value }.collect { target ->
            launch {
                letterAbsOffset.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = 0.35f, // Very bouncy
                        stiffness = Spring.StiffnessLow // Moves slowly
                    )
                )
            }
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    // Convert margins to px properly using density to avoid being too small on high-density screens
    val safeMarginX = with(density) { 80.dp.toPx() }
    val safeMarginTop = with(density) { 140.dp.toPx() }
    
    // In both LTR and RTL, moving towards Start means negative offset. 
    val xBounds = (-screenWidthPx + safeMarginX)..0f

    Box(
        modifier = modifier
            .offset { IntOffset(fabOffsetState.value.x.roundToInt(), fabOffsetState.value.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val current = fabOffsetState.value
                    
                    // In RTL, positive dragAmount.x (swiping right) means moving towards Start.
                    // Modifier.offset in RTL moves left for positive x. So we must invert dragAmount.x.
                    val deltaX = if (layoutDirection == LayoutDirection.Rtl) -dragAmount.x else dragAmount.x
                    
                    val newX = (current.x + deltaX).coerceIn(xBounds.start, xBounds.endInclusive)
                    val newY = (current.y + dragAmount.y).coerceIn(-screenHeightPx + safeMarginTop, 0f)
                    fabOffsetState.value = Offset(newX, newY)
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = floatY.dp.toPx()
                shadowElevation = 16.dp.toPx()
                shape = CircleShape
                spotShadowColor = primary
                ambientShadowColor = secondary
                clip = false
            }
            .size(64.dp)
            .drawBehind {
                rotate(angle) {
                    drawCircle(
                        brush = Brush.sweepGradient(aiGradient, center = Offset(size.width/2f, size.height/2f)),
                        radius = size.width / 2f,
                        center = Offset(size.width/2f, size.height/2f)
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick() }
            )
            .padding(3.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val textGradient = Brush.linearGradient(
            colors = listOf(primary, tertiary)
        )
        Text(
            text = stringResource(R.string.chat_fab_icon),
            modifier = Modifier.graphicsLayer {
                val currentFab = fabOffsetState.value
                val currentAbs = letterAbsOffset.value
                val relX = currentAbs.x - currentFab.x
                val relY = currentAbs.y - currentFab.y

                val maxDist = 12.dp.toPx()
                val currentDist = kotlin.math.sqrt((relX * relX + relY * relY).toDouble()).toFloat()

                if (currentDist > maxDist && currentDist > 0f) {
                    val ratio = maxDist / currentDist
                    translationX = relX * ratio
                    translationY = relY * ratio
                } else {
                    translationX = relX
                    translationY = relY
                }
            },
            style = TextStyle(
                brush = textGradient,
                fontSize = 36.sp, // Scaled down to fit 64dp size
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = secondary.copy(alpha = 0.7f),
                    offset = Offset(0f, 6f),
                    blurRadius = 12f
                )
            )
        )
    }
}
