package com.dukkan.product_details.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
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
import kotlinx.coroutines.isActive

@Composable
fun AnimatedAiIcon(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val targetPulse = if (isLoading) 1.1f else 1.03f
    val pulseTarget by animateFloatAsState(targetValue = targetPulse, label = "PulseTargetAnim")
    val speedTarget = if (isLoading) 360f / 1200f else 360f / 5000f
    val rotationSpeed by animateFloatAsState(targetValue = speedTarget, label = "RotationSpeedAnim")
    val pulseSpeedTarget = if (isLoading) 1f / 600f else 1f / 2000f
    val pulseSpeed by animateFloatAsState(targetValue = pulseSpeedTarget, label = "PulseSpeedAnim")

    var rotation by remember { mutableFloatStateOf(0f) }
    var pulsePhase by remember { mutableFloatStateOf(0f) }
    var pulseDir by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        var lastTime = withFrameMillis { it }
        while (isActive) {
            val currentTime = withFrameMillis { it }
            val delta = currentTime - lastTime
            lastTime = currentTime

            rotation = (rotation + rotationSpeed * delta) % 360f

            pulsePhase += pulseDir * pulseSpeed * delta
            if (pulsePhase >= 1f) {
                pulsePhase = 1f
                pulseDir = -1
            } else if (pulsePhase <= 0f) {
                pulsePhase = 0f
                pulseDir = 1
            }
        }
    }

    val easedPhase = FastOutSlowInEasing.transform(pulsePhase)
    val pulseScale = 1f + easedPhase * (pulseTarget - 1f)

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
                quadraticTo(cx, cy, size.width, cy)
                quadraticTo(cx, cy, cx, size.height)
                quadraticTo(cx, cy, 0f, cy)
                quadraticTo(cx, cy, cx, 0f)
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
