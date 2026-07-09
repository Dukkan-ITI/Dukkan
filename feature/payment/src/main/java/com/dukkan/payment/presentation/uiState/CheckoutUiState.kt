package com.dukkan.payment.presentation.uiState

import androidx.compose.runtime.Immutable
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.domain.model.PaymobCredentials
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.cart.CartSummary
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.payment.presentation.UiText
import com.google.android.gms.maps.model.LatLng

@Immutable
internal data class CheckoutUiState(
    val isLoadingCartOrAddresses: Boolean = false,
    val cartSummary: CartSummary? = null,
    val storeCart: StoreCart? = null,
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val selectedMethod: PaymentMethod? = null,
    val isCreatingIntention: Boolean = false,
    val isOnline: Boolean = true,
    val showOfflinePopup: Boolean = false,
    val showOfflineToast: Boolean = false,
    val paymobCredentials: PaymobCredentials? = null,
    val result: OrderResult? = null,
    val error: UiText? = null,
    // Address selection / add-new flow
    val isAddressSheetVisible: Boolean = false,
    val isAddressFormVisible: Boolean = false,
    val isSavingAddress: Boolean = false,
    val addressFormError: String? = null,
    val isMapVisible: Boolean = false,
    val selectedLatLng: LatLng? = null,
)

internal sealed interface OrderResult {
    data class Success(val confirmation: OrderConfirmation) : OrderResult
    data class Failure(val reason: UiText, val canRetry: Boolean) : OrderResult
    data class Pending(val confirmation: OrderConfirmation?) : OrderResult
}
