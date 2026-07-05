package com.dukkan.payment.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent

import androidx.compose.animation.core.tween

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.components.AddressSection
import com.dukkan.payment.presentation.components.MethodSection
import com.dukkan.payment.presentation.components.OrderSummaryBar
import com.dukkan.payment.presentation.components.PaymentResultOverlay
import com.dukkan.payment.presentation.components.PaymobSdkLauncher
import com.dukkan.payment.presentation.components.PaymobThemeColors




// i know that i use static colors , don't comment here

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

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutEffect.Finish -> onPaymentResult(effect.result)
            }
        }
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

    
    uiState.result?.let { result ->
        PaymentResultOverlay(
            result = result,
            cartSummary = uiState.cartSummary,
            paymentMethod = uiState.selectedMethod,
            onRetry = { onEvent(CheckoutEvent.Retry) },
            onResultAcknowledged = { isPending -> onEvent(CheckoutEvent.AcknowledgeResult(isPending)) },
            onDismiss = { onEvent(CheckoutEvent.DismissResult) }
        )
    }

    
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
                
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator() 
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



private tailrec fun android.content.Context.findActivity(): AppCompatActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
