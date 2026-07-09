package com.dukkan.payment.presentation.uiState

import com.dukkan.domain.model.Address
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.components.PaymobSdkStatus
import com.google.android.gms.maps.model.LatLng

internal sealed interface CheckoutEvent {
    data class SelectAddress(val addressId: String) : CheckoutEvent
    data class SelectMethod(val method: PaymentMethod) : CheckoutEvent

    // Address selection sheet
    object OpenAddressSheet : CheckoutEvent
    object DismissAddressSheet : CheckoutEvent

    // Add-new-address form
    object OpenAddNewAddress : CheckoutEvent
    object DismissAddressForm : CheckoutEvent
    data class SaveNewAddress(val address: Address) : CheckoutEvent

    // Map picker
    object OpenMap : CheckoutEvent
    object DismissMap : CheckoutEvent
    data class LocationSelected(val latLng: LatLng) : CheckoutEvent
    object ClearSelectedLatLng : CheckoutEvent

    object SubmitOrder : CheckoutEvent
    data class PaymobSdkFinished(
        val status: PaymobSdkStatus,
        val message: String?
    ) : CheckoutEvent

    object CancelPaymentFlow : CheckoutEvent
    object AppResumedDuringPayment : CheckoutEvent
    object Retry : CheckoutEvent
    object DismissResult : CheckoutEvent
    data class AcknowledgeResult(val isPending: Boolean) : CheckoutEvent
}
