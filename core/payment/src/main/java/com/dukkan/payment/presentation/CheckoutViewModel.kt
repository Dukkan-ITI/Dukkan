package com.dukkan.payment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.domain.model.PaymobCredentials
import com.dukkan.payment.domain.usecase.ConfirmCashOrderUseCase
import com.dukkan.payment.domain.usecase.CreatePaymentIntentionUseCase
import com.dukkan.payment.domain.usecase.VerifyPaymentStatusUseCase
import com.google.gson.Gson
import com.msayeh.domain.model.Address
import com.msayeh.domain.model.OrderConfirmation
import com.msayeh.domain.model.cart.CartSummary
import com.msayeh.domain.usecase.address.GetAddressesUseCase
import com.msayeh.domain.usecase.cart.GetCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class CheckoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val confirmCashOrderUseCase: ConfirmCashOrderUseCase,
    private val createPaymentIntentionUseCase: CreatePaymentIntentionUseCase,
    private val verifyPaymentStatusUseCase: VerifyPaymentStatusUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
) : ViewModel() {

    private companion object {
        const val KEY_ORDER_ID               = "checkout_order_id"
        const val KEY_IDEMPOTENCY_KEY        = "checkout_idempotency_key"
        const val KEY_SAVED_ADDRESS_ID       = "checkout_saved_address_id"
        const val KEY_ONE_OFF_ADDRESS_JSON   = "checkout_one_off_address_json"
        const val KEY_SELECTED_METHOD        = "checkout_selected_method"
        
        const val MAX_POLL_ATTEMPTS          = 13
        const val POLL_DELAY_MS              = 1_500L
    }

    private val gson = Gson()

    private var orderId: String?
        get() = savedStateHandle[KEY_ORDER_ID]
        set(value) { savedStateHandle[KEY_ORDER_ID] = value }

    private var idempotencyKey: String
        get() = savedStateHandle[KEY_IDEMPOTENCY_KEY] ?: mintNewIdempotencyKey()
        set(value) { savedStateHandle[KEY_IDEMPOTENCY_KEY] = value }

    private var persistedMethod: PaymentMethod?
        get() = savedStateHandle.get<String>(KEY_SELECTED_METHOD)?.let {
            runCatching { PaymentMethod.valueOf(it) }.getOrNull()
        }
        set(value) { savedStateHandle[KEY_SELECTED_METHOD] = value?.name }

    private var persistedAddress: CheckoutAddress?
        get() {
            val savedId = savedStateHandle.get<String>(KEY_SAVED_ADDRESS_ID)
            if (savedId != null) return CheckoutAddress.Saved(savedId)
            
            val oneOffJson = savedStateHandle.get<String>(KEY_ONE_OFF_ADDRESS_JSON)
            if (oneOffJson != null) {
                return runCatching { CheckoutAddress.OneOff(gson.fromJson(oneOffJson, Address::class.java)) }.getOrNull()
            }
            return null
        }
        set(value) {
            when (value) {
                is CheckoutAddress.Saved -> {
                    savedStateHandle[KEY_SAVED_ADDRESS_ID] = value.addressId
                    savedStateHandle.remove<String>(KEY_ONE_OFF_ADDRESS_JSON)
                }
                is CheckoutAddress.OneOff -> {
                    savedStateHandle.remove<String>(KEY_SAVED_ADDRESS_ID)
                    savedStateHandle[KEY_ONE_OFF_ADDRESS_JSON] = gson.toJson(value.address)
                }
                null -> {
                    savedStateHandle.remove<String>(KEY_SAVED_ADDRESS_ID)
                    savedStateHandle.remove<String>(KEY_ONE_OFF_ADDRESS_JSON)
                }
            }
        }

    private val _uiState = MutableStateFlow(
        CheckoutUiState(
            selectedAddress = persistedAddress,
            selectedMethod  = persistedMethod,
        )
    )
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCartAndAddresses()
    }

    private fun loadCartAndAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCartOrAddresses = true) }

            val cart = getCartUseCase()
            val cartSummary = cart?.let {
                CartSummary(
                    cartId    = it.id,
                    total     = it.cost.totalAmount,
                    lineCount = it.lines.size,
                )
            }

            val addresses = getAddressesUseCase().getOrElse { emptyList() }
            
            
            if (persistedAddress == null && addresses.isNotEmpty()) {
                val defaultAddress = addresses.firstOrNull { it.isDefault } ?: addresses.first()
                defaultAddress.id?.let { id ->
                    val defaultSaved = CheckoutAddress.Saved(id)
                    persistedAddress = defaultSaved
                    _uiState.update { it.copy(selectedAddress = defaultSaved) }
                }
            }

            _uiState.update {
                it.copy(
                    isLoadingCartOrAddresses = false,
                    cartSummary = cartSummary,
                    addresses   = addresses,
                )
            }
        }
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.SelectAddress -> {
                persistedAddress = event.address
                _uiState.update { it.copy(selectedAddress = event.address, isEditingAddress = false) }
            }

            is CheckoutEvent.SelectMethod -> {
                persistedMethod = event.method
                _uiState.update { it.copy(selectedMethod = event.method) }
            }
            
            is CheckoutEvent.SetEditingAddress -> {
                _uiState.update { it.copy(isEditingAddress = event.isEditing) }
            }

            CheckoutEvent.SubmitOrder             -> submitOrder()
            is CheckoutEvent.PaymobSdkFinished    -> handlePaymobSdkFinished(event.status, event.message)
            CheckoutEvent.CancelPaymentFlow       -> {
                
            }
            CheckoutEvent.AppResumedDuringPayment -> onAppResumedDuringPayment()
            
            CheckoutEvent.Retry -> {
                _uiState.update { it.copy(error = null, result = null) }
            }
            
            CheckoutEvent.DismissResult -> {
                _uiState.update { it.copy(result = null) }
            }
        }
    }

    private fun submitOrder() {
        val address = _uiState.value.selectedAddress ?: return
        val method  = _uiState.value.selectedMethod ?: return
        val cartSummary = _uiState.value.cartSummary ?: return
        val cartId  = cartSummary.cartId
        val cartTotal = cartSummary.total

        when (method) {
            PaymentMethod.CASH -> confirmCash(address, cartId, cartTotal)
            PaymentMethod.ONLINE -> startOnlinePayment(address, cartId, cartTotal)
        }
    }

    private fun confirmCash(address: CheckoutAddress, cartId: String, cartTotal: com.msayeh.domain.model.Money) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntention = true, error = null) }
            val result = confirmCashOrderUseCase(
                idempotencyKey = idempotencyKey,
                address        = address,
                cartId         = cartId,
                cartTotal      = cartTotal,
            )
            _uiState.update { it.copy(isCreatingIntention = false) }
            
            result.fold(
                onSuccess = { conf ->
                    _uiState.update { it.copy(cashSuccessConfirmation = conf) }
                },
                onFailure = { e ->
                    
                    _uiState.update { it.copy(result = OrderResult.Failure(e.message ?: "Unknown error", canRetry = true)) }
                }
            )
        }
    }

    private fun startOnlinePayment(address: CheckoutAddress, cartId: String, cartTotal: com.msayeh.domain.model.Money) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntention = true, error = null) }
            val result = createPaymentIntentionUseCase(
                idempotencyKey = idempotencyKey,
                address        = address,
                cartId         = cartId,
                cartTotal      = cartTotal,
            )
            result.fold(
                onSuccess = { intention ->
                    orderId = intention.orderId
                    _uiState.update {
                        it.copy(
                            isCreatingIntention = false,
                            paymobCredentials = PaymobCredentials(
                                clientSecret = intention.clientSecret,
                                publicKey    = intention.publicKey,
                            ),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isCreatingIntention = false, 
                            result = OrderResult.Failure(e.message ?: "Failed to initialize payment", canRetry = true)
                        ) 
                    }
                },
            )
        }
    }

    private fun handlePaymobSdkFinished(status: com.dukkan.payment.presentation.components.PaymobSdkStatus, message: String?) {
        when (status) {
            com.dukkan.payment.presentation.components.PaymobSdkStatus.SUCCESS -> verifyStatus()
            com.dukkan.payment.presentation.components.PaymobSdkStatus.PENDING -> {
                _uiState.update { it.copy(error = "Payment is pending") }
            }
            com.dukkan.payment.presentation.components.PaymobSdkStatus.FAILED -> {
                val errorMsg = message ?: "Payment was cancelled or failed."
                _uiState.update { it.copy(result = OrderResult.Failure(errorMsg, canRetry = true)) }
            }
        }
    }

    private fun onAppResumedDuringPayment() {
        if (orderId != null && _uiState.value.result == null) {
            verifyStatus()
        }
    }

    private fun verifyStatus() {
        val id = orderId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(result = OrderResult.Pending(null)) }

            var attempts = 0
            var lastResult: Result<OrderConfirmation>

            do {
                delay(POLL_DELAY_MS)
                lastResult = verifyPaymentStatusUseCase(id)
                attempts++
            } while (
                lastResult.getOrNull()?.status?.equals("pending", ignoreCase = true) == true
                && attempts < MAX_POLL_ATTEMPTS
            )

            val confirmation = lastResult.getOrNull()
            when {
                confirmation != null && !confirmation.status.equals("pending", ignoreCase = true) ->
                    handleResult(lastResult)

                confirmation?.status?.equals("pending", ignoreCase = true) == true -> {
                    _uiState.update {
                        it.copy(result = OrderResult.Pending(confirmation))
                    }
                }

                else -> handleResult(lastResult)
            }
        }
    }

    private fun handleResult(result: Result<OrderConfirmation>) {
        result.fold(
            onSuccess = { confirmation ->
                val isSuccess = confirmation.status.equals("paid", ignoreCase = true) ||
                                confirmation.status.equals("success", ignoreCase = true) ||
                                confirmation.status.equals("successful", ignoreCase = true)
                
                if (isSuccess) {
                    _uiState.update {
                        it.copy(result = OrderResult.Success(confirmation))
                    }
                } else {
                    _uiState.update {
                        it.copy(result = OrderResult.Failure("Payment status: ${confirmation.status}", canRetry = true))
                    }
                }
            },
            onFailure = { e ->
                _uiState.update { 
                    it.copy(result = OrderResult.Failure(e.message ?: "Payment failed", canRetry = true)) 
                }
            },
        )
    }

    private fun mintNewIdempotencyKey(): String {
        val key = UUID.randomUUID().toString()
        savedStateHandle[KEY_IDEMPOTENCY_KEY] = key
        return key
    }
}
