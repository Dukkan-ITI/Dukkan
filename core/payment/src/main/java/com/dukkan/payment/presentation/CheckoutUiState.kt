package com.dukkan.payment.presentation

import androidx.compose.runtime.Immutable
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.domain.model.PaymobCredentials
import com.msayeh.domain.model.Address
import com.msayeh.domain.model.cart.CartSummary
import com.msayeh.domain.model.OrderConfirmation

@Immutable
internal data class CheckoutUiState(
    val isLoadingCartOrAddresses: Boolean = false,
    val cartSummary: CartSummary? = null,
    val addresses: List<Address> = emptyList(),
    val selectedAddress: CheckoutAddress? = null,
    val isEditingAddress: Boolean = false,
    val selectedMethod: PaymentMethod? = null,
    val isCreatingIntention: Boolean = false,
    val paymobCredentials: PaymobCredentials? = null,
    val result: OrderResult? = null,
    val cashSuccessConfirmation: OrderConfirmation? = null,
    val error: UiText? = null,
)

internal sealed interface OrderResult {
    data class Success(val confirmation: OrderConfirmation) : OrderResult
    data class Failure(val reason: UiText, val canRetry: Boolean) : OrderResult
    data class Pending(val confirmation: OrderConfirmation?) : OrderResult
}
