package com.dukkan.payment.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dukkan.design_system.theme.BricolageGrotesque
import com.dukkan.domain.model.asString
import com.dukkan.domain.model.cart.CartSummary
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.uiState.OrderResult

@Composable
internal fun PaymentResultOverlay(
    result: OrderResult,
    cartSummary: CartSummary?,
    paymentMethod: PaymentMethod?,
    onRetry: () -> Unit,
    onResultAcknowledged: (isPending: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    when (result) {
        is OrderResult.Failure -> FailureScreen(result = result, onRetry = onRetry, onDismiss = onDismiss)
        is OrderResult.Success -> ConfirmationScreen(
            isPending = false,
            orderId = result.confirmation.orderId,
            totalText = result.confirmation.total.asString(),
            paymentMethod = paymentMethod,
            onContinue = { onResultAcknowledged(false) },
        )
        is OrderResult.Pending -> ConfirmationScreen(
            isPending = true,
            orderId = result.confirmation?.orderId,
            totalText = result.confirmation?.total?.asString() ?: cartSummary?.total?.asString() ?: "—",
            paymentMethod = paymentMethod,
            onContinue = { onResultAcknowledged(true) },
        )
    }
}

@Composable
private fun ConfirmationScreen(
    isPending: Boolean,
    orderId: String?,
    totalText: String,
    paymentMethod: PaymentMethod?,
    onContinue: () -> Unit,
) {
    val badgeScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        badgeScale.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
        contentAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    val infiniteTransition = rememberInfiniteTransitionSafe()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ), label = "pulseAlpha"
    )

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    if (!isPending) {
                        Box(modifier = Modifier.size(88.dp).graphicsLayer {
                            scaleX = pulseScale; scaleY = pulseScale; alpha = pulseAlpha * badgeScale.value
                        }.background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                    Box(modifier = Modifier.size(88.dp).graphicsLayer {
                        scaleX = badgeScale.value; scaleY = badgeScale.value
                    }.background(if (isPending) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(imageVector = if (isPending) Icons.Default.HourglassEmpty else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(42.dp))
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = stringResource(if (isPending) R.string.payment_receipt_pending else R.string.payment_receipt_successful),
                    fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center, modifier = Modifier.alpha(contentAlpha.value)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isPending) stringResource(R.string.payment_pending_message, orderId ?: "—")
                    else stringResource(R.string.payment_success_message, orderId ?: "—"),
                    fontSize = 14.5.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.alpha(contentAlpha.value)
                )

                Spacer(modifier = Modifier.height(26.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().graphicsLayer {
                        translationY = (1f - contentAlpha.value) * 50f
                        alpha = contentAlpha.value
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.payment_total_paid), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = totalText, fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (isPending) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(text = stringResource(R.string.payment_verifying), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(text = stringResource(R.string.payment_email_sent), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp).alpha(contentAlpha.value),
                ) {
                    Text(text = stringResource(R.string.payment_continue_shopping), fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                }
            }
        }
    }
}

@Composable
private fun FailureScreen(
    result: OrderResult.Failure,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val iconScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
        contentAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(34.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            }
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(42.dp),
                        )
                    }

                    Text(
                        text = stringResource(R.string.payment_failed_title),
                        fontFamily = BricolageGrotesque,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(contentAlpha.value)
                    )

                    Text(
                        text = result.reason.asString(),
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(contentAlpha.value)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (result.canRetry) {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(56.dp).alpha(contentAlpha.value),
                        ) {
                            Text(text = stringResource(R.string.payment_retry), fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp).alpha(contentAlpha.value),
                    ) {
                        Text(stringResource(R.string.payment_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberInfiniteTransitionSafe() =
    androidx.compose.animation.core.rememberInfiniteTransition(label = "paymentResultPulse")