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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.address_ui.AddressFormSheet
import com.dukkan.address_ui.LocationPickerScreen
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.R
import com.dukkan.payment.presentation.viewModel.CheckoutEffect
import com.dukkan.payment.presentation.uiState.CheckoutEvent
import com.dukkan.payment.presentation.uiState.CheckoutUiState
import com.dukkan.payment.presentation.viewModel.CheckoutViewModel
import com.dukkan.payment.presentation.components.AddressSection
import com.dukkan.payment.presentation.components.AddressSelectSheet
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

    if (uiState.showOfflinePopup) {
        com.dukkan.design_system.components.OfflineDialog(
            onDismiss = { onEvent(CheckoutEvent.DismissOfflinePopup) }
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
        onBack = { onPaymentResult(PaymentResult.Cancelled) },
    )

    com.dukkan.design_system.components.OfflineToast(visible = uiState.showOfflineToast)
}


@Composable
internal fun CheckoutContent(
    uiState: CheckoutUiState,
    onEvent: (CheckoutEvent) -> Unit,
    onBack: () -> Unit,
) {
    if (uiState.isMapVisible) {
        LocationPickerScreen(
            onLocationSelected = { onEvent(CheckoutEvent.LocationSelected(it)) },
            onCancel = { onEvent(CheckoutEvent.DismissMap) },
        )
        return
    }

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
                            selectedAddressId = uiState.selectedAddressId,
                            addresses = uiState.addresses,
                            onEditClick = { onEvent(CheckoutEvent.OpenAddressSheet) },
                            enabled = uiState.isOnline,
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

        val isCtaEnabled = uiState.selectedAddressId != null &&
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

    if (uiState.isAddressSheetVisible) {
        AddressSelectSheet(
            addresses = uiState.addresses,
            selectedAddressId = uiState.selectedAddressId,
            onSelect = { onEvent(CheckoutEvent.SelectAddress(it)) },
            onAddNew = { onEvent(CheckoutEvent.OpenAddNewAddress) },
            onDismissRequest = { onEvent(CheckoutEvent.DismissAddressSheet) },
        )
    }

    if (uiState.isAddressFormVisible) {
        AddressFormSheet(
            initialAddress = null,
            isSaving = uiState.isSavingAddress,
            formError = uiState.addressFormError,
            selectedLatLng = uiState.selectedLatLng,
            onMapClick = { onEvent(CheckoutEvent.OpenMap) },
            onClearSelectedLatLng = { onEvent(CheckoutEvent.ClearSelectedLatLng) },
            onDismissRequest = { onEvent(CheckoutEvent.DismissAddressForm) },
            onSave = { onEvent(CheckoutEvent.SaveNewAddress(it)) },
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