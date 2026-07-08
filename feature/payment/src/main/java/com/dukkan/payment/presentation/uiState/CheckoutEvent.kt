package com.dukkan.payment.presentation.uiState

import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.presentation.components.PaymobSdkStatus

internal sealed interface CheckoutEvent {
    data class SelectAddress(val address: CheckoutAddress) : CheckoutEvent
    data class SelectMethod(val method: PaymentMethod) : CheckoutEvent
    
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
