package com.dukkan.shopping_cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.domain.model.CartItem
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.cart.AddToCartUseCase
import com.dukkan.domain.usecase.cart.CalculateCartTotalsUseCase
import com.dukkan.domain.usecase.cart.GetCartItemsUseCase
import com.dukkan.domain.usecase.cart.RefreshCartPricesUseCase
import com.dukkan.domain.usecase.cart.RemoveFromCartUseCase
import com.dukkan.domain.usecase.cart.UpdateCartQuantityUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val calculateCartTotalsUseCase: CalculateCartTotalsUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val refreshCartPricesUseCase: RefreshCartPricesUseCase,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _state = MutableStateFlow(ShoppingCartState())
    val state: StateFlow<ShoppingCartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoggedIn.value = getCurrentUser() != null
        }
        viewModelScope.launch {
            combine(getCartItemsUseCase(), getCurrencyUseCase()) { items, currency -> items to currency }
                .collectLatest { (items, currency) ->
                    val totals = calculateCartTotalsUseCase(items, currency)
                    _state.update {
                        it.copy(
                            cartItems = items,
                            subtotal = totals.subtotal,
                            shipping = totals.shipping,
                            total = totals.total
                        )
                    }

                    val stale = items.filter { it.price.currencyCode != currency.code }
                    if (stale.isEmpty()) {
                        _state.update { it.copy(pricesOutdated = false) }
                    } else {
                        val result = refreshCartPricesUseCase(stale, currency)
                        _state.update { it.copy(pricesOutdated = result.isFailure) }
                    }
                }
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            updateCartQuantityUseCase(cartItem, newQuantity)
        }
    }

    fun showRemoveDialog(cartItem: CartItem) {
        _state.update { it.copy(showRemoveDialogForItem = cartItem) }
    }

    fun dismissRemoveDialog() {
        _state.update { it.copy(showRemoveDialogForItem = null) }
    }

    fun confirmRemoveItem() {
        _state.value.showRemoveDialogForItem?.let { item ->
            viewModelScope.launch {
                removeFromCartUseCase(item.id)
                dismissRemoveDialog()
            }
        }
    }

    fun onPromoCodeChange(code: String) {
        _state.update { it.copy(promoCode = code) }
    }

    fun applyPromoCode() {
        val code = _state.value.promoCode
        if (code.isBlank()) return

        _state.update { it.copy(isApplyingPromo = true, promoError = null) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            if (code == "JUNO20") {
                _state.update { it.copy(isApplyingPromo = false, promoError = null) }
            } else {
                _state.update {
                    it.copy(
                        isApplyingPromo = false,
                        promoError = "Invalid Promo Code"
                    )
                }
            }
        }
    }

}
