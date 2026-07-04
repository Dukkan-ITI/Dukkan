package com.dukkan.auth.components


import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.theme.AppTheme

@Composable
fun AuthLoadingScreen(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_scale",
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_rotation",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "j",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                )
            }

            if (label != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Loading — no label")
@Composable
private fun AuthLoadingScreenNoLabelPreview() {
    AppTheme {
        AuthLoadingScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Loading — sign in")
@Composable
private fun AuthLoadingScreenSignInPreview() {
    AppTheme {
        AuthLoadingScreen(label = "Signing in…")
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Loading — register")
@Composable
private fun AuthLoadingScreenRegisterPreview() {
    AppTheme {
        AuthLoadingScreen(label = "Creating your account…")
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Loading — dark")
@Composable
private fun AuthLoadingScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        AuthLoadingScreen(label = "Signing in…")
    }
}