package com.dukkan.payment.presentation.view

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.presentation.AddressEditSheet
import com.dukkan.payment.presentation.viewModel.CheckoutEffect
import com.dukkan.payment.presentation.uiState.CheckoutEvent
import com.dukkan.payment.presentation.uiState.CheckoutUiState
import com.dukkan.payment.presentation.viewModel.CheckoutViewModel
import com.dukkan.payment.presentation.components.AddressSection
import com.dukkan.payment.presentation.components.CheckoutHeader
import com.dukkan.payment.presentation.components.MethodSection
import com.dukkan.payment.presentation.components.OrderSummaryBar
import com.dukkan.payment.presentation.components.OrderTotalsCard
import com.dukkan.payment.presentation.components.PaymentResultOverlay
import com.dukkan.payment.presentation.components.PaymobSdkLauncher
import com.dukkan.payment.presentation.components.PaymobThemeColors

@Composable
internal fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onPaymentResult: (PaymentResult) -> Unit,
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

    BackHandler {
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

    
    var isEditingAddress by remember { mutableStateOf(false) }

    if (isEditingAddress) {
        AddressEditSheet(
            currentAddress = if (uiState.selectedAddress is CheckoutAddress.Saved) {
                val saved = uiState.selectedAddress as CheckoutAddress.Saved
                uiState.addresses.find { it.id == saved.addressId }
            } else (uiState.selectedAddress as? CheckoutAddress.OneOff)?.address,
            onDismiss = { isEditingAddress = false },
            onSave = { newAddress ->
                onEvent(CheckoutEvent.SelectAddress(CheckoutAddress.OneOff(newAddress)))
                isEditingAddress = false
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

    CheckoutContent(
        uiState = uiState,
        onEvent = onEvent,
        onEditAddress = { isEditingAddress = true },
        onBack = { onPaymentResult(PaymentResult.Cancelled) },
    )
}

@Composable
internal fun CheckoutContent(
    uiState: CheckoutUiState,
    onEvent: (CheckoutEvent) -> Unit,
    onEditAddress: () -> Unit,
    onBack: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        CheckoutHeader(
            title = stringResource(R.string.payment_title),
            onBack = onBack,
        )

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoadingCartOrAddresses) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 6.dp, bottom = 28.dp),
                    ) {
                        AddressSection(
                            selectedAddress = uiState.selectedAddress,
                            addresses = uiState.addresses,
                            onEditClick = onEditAddress,
                        )
    
                        Spacer(modifier = Modifier.height(24.dp))
    
                        MethodSection(
                            selectedMethod = uiState.selectedMethod,
                            onMethodSelect = { onEvent(CheckoutEvent.SelectMethod(it)) },
                        )
    
                        Spacer(modifier = Modifier.height(24.dp))
    
                        OrderTotalsCard(
                            cartSummary = uiState.cartSummary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                        )
                    }
                }
            }
        }

        val isCtaEnabled = uiState.selectedAddress != null &&
                uiState.selectedMethod != null &&
                !uiState.isCreatingIntention &&
                uiState.result == null &&
                uiState.cartSummary != null

        OrderSummaryBar(
            cartSummary = uiState.cartSummary,
            isLoading = uiState.isCreatingIntention,
            enabled = isCtaEnabled,
            onSubmit = { onEvent(CheckoutEvent.SubmitOrder) },
        )
    }
}

private tailrec fun Context.findActivity(): AppCompatActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}