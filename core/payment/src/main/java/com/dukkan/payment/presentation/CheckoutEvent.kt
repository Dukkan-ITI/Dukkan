package com.dukkan.payment.presentation

import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod

internal sealed interface CheckoutEvent {
    data class SelectAddress(val address: CheckoutAddress) : CheckoutEvent
    data class SelectMethod(val method: PaymentMethod) : CheckoutEvent
    data class SetEditingAddress(val isEditing: Boolean) : CheckoutEvent
    
    object SubmitOrder : CheckoutEvent
    object PaymobSdkFinished : CheckoutEvent
    object AppResumedDuringPayment : CheckoutEvent
    object Retry : CheckoutEvent
    object DismissResult : CheckoutEvent
}
