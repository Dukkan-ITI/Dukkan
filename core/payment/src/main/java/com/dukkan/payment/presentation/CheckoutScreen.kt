package com.dukkan.payment.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.components.MethodTile
import com.dukkan.payment.presentation.components.OrderSummaryBar
import com.dukkan.payment.presentation.components.PaymobSdkLauncher
import com.dukkan.payment.presentation.components.PaymobThemeColors
import com.msayeh.domain.model.Address
import com.msayeh.domain.model.OrderConfirmation
import com.msayeh.domain.model.asString
import com.msayeh.domain.model.cart.CartSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onPaymentResult: (PaymentResult) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onEvent = remember(viewModel) { viewModel::onEvent }

    LaunchedEffect(uiState.result) {
        // We now rely entirely on the receipt's Done button to trigger onPaymentResult
    }

    androidx.activity.compose.BackHandler {
        onPaymentResult(PaymentResult.Cancelled)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onEvent(CheckoutEvent.AppResumedDuringPayment)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Address Edit Sheet Overlay
    if (uiState.isEditingAddress) {
        AddressEditSheet(
            currentAddress = (uiState.selectedAddress as? CheckoutAddress.Saved)?.let { saved ->
                uiState.addresses.find { it.id == saved.addressId }
            } ?: (uiState.selectedAddress as? CheckoutAddress.OneOff)?.address,
            onDismiss = { onEvent(CheckoutEvent.SetEditingAddress(false)) },
            onSave = { newAddress ->
                onEvent(CheckoutEvent.SelectAddress(CheckoutAddress.OneOff(newAddress)))
            }
        )
    }

    // Payment Result Overlay
    uiState.result?.let { result ->
        PaymentResultOverlay(
            result = result,
            cartSummary = uiState.cartSummary,
            paymentMethod = uiState.selectedMethod,
            onRetry = { onEvent(CheckoutEvent.Retry) },
            onResult = { onPaymentResult(it) },
            onDismiss = { onEvent(CheckoutEvent.DismissResult) }
        )
    }

    // Paymob SDK Launcher side-effect
    uiState.paymobCredentials?.let { credentials ->
        val activity = context.findActivity() as? AppCompatActivity ?: return@let
        val colorScheme = MaterialTheme.colorScheme
        val launcher = remember(credentials) {
            PaymobSdkLauncher(
                activity   = activity,
                colors     = PaymobThemeColors(
                    primary   = colorScheme.primary.toArgb(),
                    onPrimary = colorScheme.onPrimary.toArgb(),
                    surface   = colorScheme.surface.toArgb(),
                    onSurface = colorScheme.onSurface.toArgb(),
                    outline   = colorScheme.outline.toArgb(),
                    error     = colorScheme.error.toArgb(),
                ),
                onFinished = { status, msg -> 
                    onEvent(CheckoutEvent.PaymobSdkFinished(status, msg)) 
                },
            )
        }
        LaunchedEffect(credentials) {
            launcher.launch(
                clientSecret = credentials.clientSecret,
                publicKey    = credentials.publicKey,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payment_title)) },
                navigationIcon = {
                    IconButton(onClick = { onPaymentResult(PaymentResult.Cancelled) }) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = stringResource(R.string.payment_navigate_up),
                        )
                    }
                },
            )
        },
        bottomBar = {
            uiState.cartSummary?.let { summary ->
                Column {
                    OrderSummaryBar(total = summary.total, lineCount = summary.lineCount)
                    
                    val isCtaEnabled = uiState.selectedAddress != null && 
                                       uiState.selectedMethod != null && 
                                       !uiState.isCreatingIntention && 
                                       uiState.result == null

                    Button(
                        onClick = { onEvent(CheckoutEvent.SubmitOrder) },
                        enabled = isCtaEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp)
                    ) {
                        AnimatedContent(
                            targetState = uiState.isCreatingIntention,
                            label = "cta_loading",
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                fadeOut(animationSpec = tween(90))
                            }
                        ) { isLoading ->
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(stringResource(R.string.payment_confirm))
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.isLoadingCartOrAddresses) {
                // Skeleton loading state
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator() // Simplification for shimmer
                }
            } else {
                AddressSection(
                    selectedAddress = uiState.selectedAddress,
                    addresses = uiState.addresses,
                    onEditClick = { onEvent(CheckoutEvent.SetEditingAddress(true)) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MethodSection(
                    selectedMethod = uiState.selectedMethod,
                    onMethodSelect = { onEvent(CheckoutEvent.SelectMethod(it)) }
                )
            }
        }
    }
}

@Composable
private fun AddressSection(
    selectedAddress: CheckoutAddress?,
    addresses: List<Address>,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(spring(stiffness = Spring.StiffnessLow))
    ) {
        Text(
            text = stringResource(R.string.payment_step_address_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedAddress == null) {
                    Text("No address selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val actualAddress = when (selectedAddress) {
                        is CheckoutAddress.Saved -> addresses.find { it.id == selectedAddress.addressId }
                        is CheckoutAddress.OneOff -> selectedAddress.address
                    }
                    
                    if (actualAddress != null) {
                        Text(
                            text = "${actualAddress.firstName ?: ""} ${actualAddress.lastName ?: ""}".trim(),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${actualAddress.address1}, ${actualAddress.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        actualAddress.phone?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Address details unavailable")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Edit for this order")
                }
            }
        }
    }
}

@Composable
private fun MethodSection(
    selectedMethod: PaymentMethod?,
    onMethodSelect: (PaymentMethod) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.payment_step_method_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        MethodTile(
            title      = stringResource(R.string.payment_method_cash_title),
            subtitle   = stringResource(R.string.payment_method_cash_subtitle),
            icon       = Icons.Default.LocalShipping,
            isSelected = selectedMethod == PaymentMethod.CASH,
            onClick    = { onMethodSelect(PaymentMethod.CASH) },
        )

        MethodTile(
            title      = stringResource(R.string.payment_method_online_title),
            subtitle   = stringResource(R.string.payment_method_online_subtitle),
            icon       = Icons.Default.CreditCard,
            isSelected = selectedMethod == PaymentMethod.ONLINE,
            onClick    = { onMethodSelect(PaymentMethod.ONLINE) },
        )
    }
}

@Composable
private fun PaymentResultOverlay(
    result: OrderResult,
    cartSummary: CartSummary?,
    paymentMethod: PaymentMethod?,
    onRetry: () -> Unit,
    onResult: (PaymentResult) -> Unit,
    onDismiss: () -> Unit,
) {
    if (result is OrderResult.Failure) {
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
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failure",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = stringResource(R.string.payment_failed_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = result.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
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
    val orderId = when (result) {
        is OrderResult.Success -> result.confirmation.orderId
        is OrderResult.Pending -> result.confirmation?.orderId ?: "---"
        else -> "---"
    }

    val totalAmount = when (result) {
        is OrderResult.Success -> result.confirmation.total.asString()
        is OrderResult.Pending -> result.confirmation?.total?.asString() ?: cartSummary?.total?.asString() ?: "---"
        else -> "---"
    }

    val rawTotal = when (result) {
        is OrderResult.Success -> result.confirmation.total.amount.toDouble()
        is OrderResult.Pending -> result.confirmation?.total?.amount?.toDouble() ?: cartSummary?.total?.amount?.toDouble() ?: 0.0
        else -> 0.0
    }

    val currency = when (result) {
        is OrderResult.Success -> result.confirmation.total.currencyCode
        is OrderResult.Pending -> result.confirmation?.total?.currencyCode ?: cartSummary?.total?.currencyCode ?: ""
        else -> ""
    }

    val am = LocalAccessibilityManager.current
    val isReducedMotion = am?.calculateRecommendedTimeoutMillis(1000, true) == 1000L

    // Animations
    val slideAnim = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 0f else 300f) }
    val badgeScale = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }
    val badgeAlpha = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }
    val contentAlpha = remember { androidx.compose.animation.core.Animatable(if (isReducedMotion) 1f else 0f) }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ), label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            slideAnim.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f))
            kotlinx.coroutines.delay(100)
            badgeScale.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(200))
            badgeAlpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(200))
            kotlinx.coroutines.delay(150)
            contentAlpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(300))
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
                        // Badge
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isPending) {
                                // Creative Pulse background
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                            alpha = pulseAlpha * badgeAlpha.value
                                        }
                                        .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .graphicsLayer {
                                        scaleX = badgeScale.value
                                        scaleY = badgeScale.value
                                        alpha = badgeAlpha.value
                                        // Bounce rotation effect
                                        rotationZ = (1f - badgeScale.value) * -45f
                                    }
                                    .background(
                                        if (isPending) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPending) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle,
                                    contentDescription = if (isPending) "Pending" else "Success",
                                    tint = if (isPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(if (isPending) R.string.payment_receipt_pending else R.string.payment_receipt_successful),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.alpha(badgeAlpha.value)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.alpha(contentAlpha.value)) {
                            // Method
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

                            // Reference
                            Text(
                                text = stringResource(R.string.payment_reference, orderId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Items (Staggered or simple fade)
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
                                val methodStr = if (paymentMethod == PaymentMethod.CASH) "CASH" else "CARD"
                                if (isPending) {
                                    onResult(PaymentResult.Pending(orderId))
                                } else {
                                    onResult(PaymentResult.Success(orderId, rawTotal, currency, methodStr))
                                }
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

private tailrec fun android.content.Context.findActivity(): AppCompatActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
