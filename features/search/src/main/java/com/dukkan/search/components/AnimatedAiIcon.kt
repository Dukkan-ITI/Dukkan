package com.dukkan.search.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedAiIcon(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AiIconAnimation")
    
    val rotationDuration = if (isLoading) 1200 else 5000
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AiIconRotation"
    )

    val targetPulse = if (isLoading) 1.1f else 1.03f
    val pulseDuration = if (isLoading) 600 else 2000
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = targetPulse,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiIconPulse"
    )

    Box(
        modifier = modifier
            .padding(4.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            val path = Path().apply {
                moveTo(cx, 0f)
                quadraticBezierTo(cx, cy, size.width, cy)
                quadraticBezierTo(cx, cy, cx, size.height)
                quadraticBezierTo(cx, cy, 0f, cy)
                quadraticBezierTo(cx, cy, cx, 0f)
                close()
            }

            val sweepGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF4285F4),
                    Color(0xFFEA4335),
                    Color(0xFFf4a261),
                    Color(0xFFFBBC05),
                    Color(0xFF34A853),
                    Color(0xFF4285F4)
                ),
                center = Offset(cx, cy)
            )

            clipPath(path) {
                rotate(degrees = rotation, pivot = Offset(cx, cy)) {
                    drawRect(
                        brush = sweepGradient,
                        size = size
                    )
                }
            }
        }
    }
}

