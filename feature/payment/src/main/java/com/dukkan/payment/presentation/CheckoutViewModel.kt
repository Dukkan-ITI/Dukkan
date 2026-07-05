package com.dukkan.payment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.domain.model.cart.CartSummary
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.usecase.address.GetAddressesUseCase
import com.dukkan.domain.usecase.cart.GetCartUseCase
import com.dukkan.domain.usecase.customer.GetCustomerIdUseCase
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.OrderCancelReason
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import com.dukkan.payment.domain.model.OrderLineItemDraft
import com.dukkan.payment.domain.model.PaymentMethod
import com.dukkan.payment.domain.model.PaymobCredentials
import com.dukkan.payment.domain.model.ShippingLineDraft
import com.dukkan.payment.domain.usecase.CancelOrderUseCase
import com.dukkan.payment.domain.usecase.CreateOrderUseCase
import com.dukkan.payment.domain.usecase.CreatePaymentIntentionUseCase
import com.dukkan.payment.domain.usecase.MarkOrderPaidUseCase
import com.dukkan.payment.domain.usecase.VerifyPaymentStatusUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.dukkan.payment.presentation.CheckoutConstants.KEY_IDEMPOTENCY_KEY
import com.dukkan.payment.presentation.CheckoutConstants.KEY_ONE_OFF_ADDRESS_JSON
import com.dukkan.payment.presentation.CheckoutConstants.KEY_ORDER_ID
import com.dukkan.payment.presentation.CheckoutConstants.KEY_SAVED_ADDRESS_ID
import com.dukkan.payment.presentation.CheckoutConstants.KEY_SELECTED_METHOD
import com.dukkan.payment.presentation.CheckoutConstants.MAX_POLL_ATTEMPTS
import com.dukkan.payment.presentation.CheckoutConstants.POLL_DELAY_MS
import com.dukkan.payment.presentation.CheckoutConstants.STANDARD_SHIPPING_CODE
import com.dukkan.payment.presentation.CheckoutConstants.STANDARD_SHIPPING_PRICE
import com.dukkan.payment.presentation.CheckoutConstants.STANDARD_SHIPPING_TITLE

internal sealed interface CheckoutEffect {
    data class Finish(val result: PaymentResult) : CheckoutEffect
}

@HiltViewModel
internal class CheckoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createPaymentIntentionUseCase: CreatePaymentIntentionUseCase,
    private val verifyPaymentStatusUseCase: VerifyPaymentStatusUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val markOrderPaidUseCase: MarkOrderPaidUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val getCustomerIdUseCase: GetCustomerIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
) : ViewModel() {

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

    private val _effect = MutableSharedFlow<CheckoutEffect>()
    val effect = _effect.asSharedFlow()

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
                    storeCart = cart,
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
                cancelPendingOrder()
            }
            CheckoutEvent.AppResumedDuringPayment -> onAppResumedDuringPayment()
            
            CheckoutEvent.Retry -> {
                _uiState.update { it.copy(error = null, result = null) }
            }
            
            CheckoutEvent.DismissResult -> {
                _uiState.update { it.copy(result = null) }
            }
            
            is CheckoutEvent.AcknowledgeResult -> {
                val methodStr = if (_uiState.value.selectedMethod == PaymentMethod.CASH) "CASH" else "CARD"
                val resultState = _uiState.value.result
                val ordId = when (resultState) {
                    is OrderResult.Success -> resultState.confirmation.orderId
                    is OrderResult.Pending -> resultState.confirmation?.orderId ?: "---"
                    else -> "---"
                }
                val finalMoney = when (resultState) {
                    is OrderResult.Success -> resultState.confirmation.total
                    is OrderResult.Pending -> resultState.confirmation?.total ?: _uiState.value.cartSummary?.total
                    else -> _uiState.value.cartSummary?.total
                } ?: com.dukkan.domain.model.Money(java.math.BigDecimal.ZERO, "EGP")
                
                viewModelScope.launch {
                    if (event.isPending) {
                        _effect.emit(CheckoutEffect.Finish(PaymentResult.Pending(ordId)))
                    } else {
                        _effect.emit(CheckoutEffect.Finish(PaymentResult.Success(ordId, finalMoney, methodStr)))
                    }
                }
            }
        }
    }

    private fun submitOrder() {
        val address = _uiState.value.selectedAddress ?: return
        val method  = _uiState.value.selectedMethod ?: return
        val cart = _uiState.value.storeCart ?: return

        when (method) {
            PaymentMethod.CASH -> confirmCash(address, cart)
            PaymentMethod.ONLINE -> startOnlinePayment(address, cart)
        }
    }

    private fun confirmCash(address: CheckoutAddress, cart: StoreCart) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntention = true, error = null) }
            val result = buildOrderDraft(address, cart)
                .fold(
                    onSuccess = { draft -> createOrderUseCase(draft, OrderFinancialStatus.PENDING) },
                    onFailure = { Result.failure(it) },
                )
            _uiState.update { it.copy(isCreatingIntention = false) }
            
            result.fold(
                onSuccess = { createdOrder ->
                    viewModelScope.launch {
                        _effect.emit(
                            CheckoutEffect.Finish(
                                PaymentResult.Success(
                                    orderId = createdOrder.orderId,
                                    total = createdOrder.total,
                                    paymentMethod = "CASH"
                                )
                            )
                        )
                    }
                },
                onFailure = { e ->
                    
                    val errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_unknown)
                    _uiState.update { it.copy(result = OrderResult.Failure(errorMessage, canRetry = true)) }
                }
            )
        }
    }

    private fun startOnlinePayment(address: CheckoutAddress, cart: StoreCart) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntention = true, error = null) }
            val orderResult = buildOrderDraft(address, cart)
                .fold(
                    onSuccess = { draft -> createOrderUseCase(draft, OrderFinancialStatus.PENDING) },
                    onFailure = { Result.failure(it) },
                )

            val createdOrder = orderResult.getOrElse { e ->
                val errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_initialize)
                _uiState.update {
                    it.copy(
                        isCreatingIntention = false,
                        result = OrderResult.Failure(errorMessage, canRetry = true)
                    )
                }
                return@launch
            }

            orderId = createdOrder.orderId

            val result = createPaymentIntentionUseCase(
                idempotencyKey = idempotencyKey,
                address        = address,
                cartId         = cart.id,
                cartTotal      = cart.cost.totalAmount,
            )
            result.fold(
                onSuccess = { intention ->
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
                    cancelOrderUseCase(createdOrder.orderId, OrderCancelReason.OTHER)
                    val errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_initialize)
                    _uiState.update { 
                        it.copy(
                            isCreatingIntention = false, 
                            result = OrderResult.Failure(errorMessage, canRetry = true)
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
                _uiState.update { it.copy(error = UiText.StringResource(R.string.payment_error_pending)) }
            }
            com.dukkan.payment.presentation.components.PaymobSdkStatus.FAILED -> {
                val errorMsg = message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_cancelled)
                cancelPendingOrder(errorMsg)
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
                lastResult = verifyPaymentStatusUseCase(id, _uiState.value.cartSummary?.total)
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
                    markPendingOrderPaid()
                } else {
                    cancelPendingOrder(UiText.StringResource(R.string.payment_error_status, confirmation.status))
                }
            },
            onFailure = { e ->
                val errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_failed)
                cancelPendingOrder(errorMessage)
            },
        )
    }

    private fun markPendingOrderPaid() {
        val id = orderId ?: return
        viewModelScope.launch {
            val result = markOrderPaidUseCase(id)
            result.fold(
                onSuccess = { createdOrder ->
                    _uiState.update {
                        it.copy(
                            result = OrderResult.Success(
                                OrderConfirmation(
                                    orderId = createdOrder.orderId,
                                    status = createdOrder.financialStatus,
                                    total = createdOrder.total,
                                )
                            )
                        )
                    }
                },
                onFailure = { e ->
                    val errorMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.payment_error_failed)
                    _uiState.update {
                        it.copy(
                            result = OrderResult.Failure(errorMessage, canRetry = true),
                        )
                    }
                },
            )
        }
    }

    private fun cancelPendingOrder(
        message: UiText = UiText.StringResource(R.string.payment_error_cancelled),
    ) {
        val id = orderId
        viewModelScope.launch {
            if (id != null) {
                cancelOrderUseCase(id, OrderCancelReason.OTHER)
            }
            _uiState.update { it.copy(result = OrderResult.Failure(message, canRetry = true)) }
        }
    }

    private suspend fun buildOrderDraft(address: CheckoutAddress, cart: StoreCart): Result<OrderDraft> =
        getCustomerIdUseCase().mapCatching { customerId ->
            val cartTotal = cart.cost.totalAmount
            OrderDraft(
                customerId = customerId,
                lineItems = cart.lines.map {
                    OrderLineItemDraft(
                        variantId = it.merchandise.id,
                        quantity = it.quantity,
                    )
                },
                // ponytail: fixed shipping because the storefront cart has no shipping line in this flow.
                shippingLine = ShippingLineDraft(
                    title = STANDARD_SHIPPING_TITLE,
                    code = STANDARD_SHIPPING_CODE,
                    price = Money(STANDARD_SHIPPING_PRICE, cartTotal.currencyCode),
                ),
                currency = cartTotal.currencyCode,
                shippingAddress = address.toAddress(),
            )
        }

    private fun CheckoutAddress.toAddress(): Address? =
        when (this) {
            is CheckoutAddress.OneOff -> address
            is CheckoutAddress.Saved -> _uiState.value.addresses.firstOrNull { it.id == addressId }
        }

    private fun mintNewIdempotencyKey(): String {
        val key = UUID.randomUUID().toString()
        savedStateHandle[KEY_IDEMPOTENCY_KEY] = key
        return key
    }
}
