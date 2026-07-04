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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onOrderConfirmed: (OrderConfirmation) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onEvent = remember(viewModel) { viewModel::onEvent }

    LaunchedEffect(uiState.result) {
        val result = uiState.result
        if (result is OrderResult.Success) {
            // Usually we might wait for user to dismiss the overlay, but if we want auto-navigate:
            // onOrderConfirmed(result.confirmation)
            // But requirement says: "Keep the result overlay dismissible only via its own explicit actions"
        }
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
            onRetry = { onEvent(CheckoutEvent.Retry) },
            onDone = { 
                if (result is OrderResult.Success) {
                    onOrderConfirmed(result.confirmation)
                } else if (result is OrderResult.Pending) {
                    // Navigate up or to order tracking. For now we just dismiss or navigate up.
                    onNavigateUp()
                } else if (result is OrderResult.Failure) {
                    onEvent(CheckoutEvent.DismissResult)
                }
            }
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
                onFinished = { onEvent(CheckoutEvent.PaymobSdkFinished) },
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
                    IconButton(onClick = onNavigateUp) {
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
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
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
                    AnimatedContent(
                        targetState = result,
                        label = "result_icon_animation",
                        transitionSpec = {
                            scaleIn(animationSpec = tween(400)) togetherWith scaleOut(animationSpec = tween(400))
                        }
                    ) { state ->
                        when (state) {
                            is OrderResult.Success -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                            is OrderResult.Failure -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Failure",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                            is OrderResult.Pending -> {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Pending",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                    }

                    when (result) {
                        is OrderResult.Success -> {
                            Text(
                                text = stringResource(R.string.payment_success_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.payment_success_order_id, result.confirmation.orderId),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = result.confirmation.total.asString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                                Text("Done")
                            }
                        }
                        is OrderResult.Failure -> {
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
                            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                                Text("Cancel")
                            }
                        }
                        is OrderResult.Pending -> {
                            Text(
                                text = stringResource(R.string.payment_still_pending_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.payment_still_pending_body),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.payment_track_order))
                            }
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
