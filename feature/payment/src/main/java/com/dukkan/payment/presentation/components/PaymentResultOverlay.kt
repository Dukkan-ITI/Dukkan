package com.dukkan.payment.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dukkan.domain.model.asString
import com.dukkan.domain.model.cart.CartSummary
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.OrderResult

@Composable
internal fun PaymentResultOverlay(
    result: OrderResult,
    cartSummary: CartSummary?,
    paymentMethod: PaymentMethod?,
    onRetry: () -> Unit,
    onResultAcknowledged: (isPending: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (result is OrderResult.Failure) {
        Dialog(
            onDismissRequest = {  },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "errorPulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "errorPulseAnim"
                        )

                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failure",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                        Text(
                            text = stringResource(R.string.payment_failed_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = result.reason.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        if (result.canRetry) {
                            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.payment_retry))
                            }
                        }
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
        return
    }

    val isPending = result is OrderResult.Pending
    
    val totalAmount = when (result) {
        is OrderResult.Success -> result.confirmation.total.asString()
        is OrderResult.Pending -> result.confirmation?.total?.asString() ?: cartSummary?.total?.asString() ?: "---"
        else -> "---"
    }

    val am = LocalAccessibilityManager.current
    val isReducedMotion = am?.calculateRecommendedTimeoutMillis(1000, true) == 1000L

    val slideAnim = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 0f else 300f) }
    val badgeScale = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }
    val badgeAlpha = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }
    val contentAlpha = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            slideAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
            kotlinx.coroutines.delay(100)
            badgeScale.animateTo(1f, animationSpec = tween(200))
            badgeAlpha.animateTo(1f, animationSpec = tween(200))
            kotlinx.coroutines.delay(150)
            contentAlpha.animateTo(1f, animationSpec = tween(300))
        }
    }

    Dialog(
        onDismissRequest = { /* Not dismissible by tap outside */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.4f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .graphicsLayer {
                            translationY = slideAnim.value
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isPending) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                            alpha = pulseAlpha * badgeAlpha.value
                                        }
                                        .background(Color(0xFF4CAF50), shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .graphicsLayer {
                                        scaleX = badgeScale.value
                                        scaleY = badgeScale.value
                                        alpha = badgeAlpha.value
                                        rotationZ = (1f - badgeScale.value) * -45f
                                    }
                                    .background(
                                        if (isPending) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFE8F5E9),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPending) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle,
                                    contentDescription = if (isPending) "Pending" else "Success",
                                    tint = if (isPending) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF2E7D32),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(if (isPending) R.string.payment_receipt_pending else R.string.payment_receipt_successful),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) MaterialTheme.colorScheme.onSurface else Color(0xFF2E7D32),
                            modifier = Modifier.alpha(badgeAlpha.value)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.alpha(contentAlpha.value)) {
                            val methodText = when (paymentMethod) {
                                PaymentMethod.CASH -> stringResource(R.string.payment_paid_by_cash)
                                PaymentMethod.ONLINE -> stringResource(R.string.payment_paid_by_card)
                                else -> "---"
                            }
                            Text(
                                text = methodText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${cartSummary?.lineCount ?: 0} items",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = totalAmount,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.payment_total),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = totalAmount,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = java.text.DateFormat.getDateTimeInstance().format(java.util.Date()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                onResultAcknowledged(isPending)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .alpha(contentAlpha.value)
                        ) {
                            Text(stringResource(R.string.payment_done))
                        }
                    }
                }
            }
        }
    }
}
