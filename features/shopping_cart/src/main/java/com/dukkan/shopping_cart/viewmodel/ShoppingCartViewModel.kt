package com.dukkan.shopping_cart.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.shopping_cart.R
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import dagger.hilt.android.qualifiers.ApplicationContext
import com.dukkan.domain.model.cart.CartLine
import com.dukkan.domain.usecase.GetCurrentUserUseCase
import com.dukkan.domain.usecase.cart.CartUseCases
import com.dukkan.domain.usecase.coupon.CouponUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(
    private val cartUseCases: CartUseCases,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val couponUseCases: CouponUseCases,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _state = MutableStateFlow(ShoppingCartState())
    val state: StateFlow<ShoppingCartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = getCurrentUser()
            _isLoggedIn.value = user != null
            if (user != null) {
                loadCart()
            }
        }
    }

    private suspend fun loadCart() {
        if (_state.value.cart == null) {
            _state.update { it.copy(isLoading = true) }
        }
        val cart = cartUseCases.getCart()
        _state.update { it.copy(cart = cart, isLoading = false) }
    }

    fun updateQuantity(cartLine: CartLine, newQuantity: Int) {
        viewModelScope.launch {
            _state.update { state ->
                val cart = state.cart ?: return@update state
                val updatedLines = cart.lines.map { line ->
                    if (line.id == cartLine.id) line.copy(quantity = newQuantity) else line
                }
                state.copy(cart = cart.copy(lines = updatedLines))
            }
            try {
                cartUseCases.updateCartQuantity(cartLine.id, newQuantity)
                refreshCart()
            } catch (e: Exception) {
                refreshCart()
            }
        }
    }

    fun onScreenEntered() {
        viewModelScope.launch {
            refreshCart()
            loadSavedCoupon()
        }
    }

    private suspend fun refreshCart() {
        val cart = cartUseCases.getCart()
        _state.update { it.copy(cart = cart) }
    }

    fun showRemoveDialog(cartLine: CartLine) {
        _state.update { it.copy(showRemoveDialogForItem = cartLine) }
    }

    fun dismissRemoveDialog() {
        _state.update { it.copy(showRemoveDialogForItem = null) }
    }

    fun confirmRemoveItem() {
        _state.value.showRemoveDialogForItem?.let { item ->
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, showRemoveDialogForItem = null) }
                cartUseCases.removeFromCart(item.id)
                loadCart()
            }
        }
    }

    fun onPromoCodeChange(code: String) {
        _state.update { it.copy(promoCode = code, promoError = null) }
    }

    fun applyPromoCode() {
        val code = _state.value.promoCode
        if (code.isBlank()) return

        _state.update { it.copy(isApplyingPromo = true, promoError = null) }
        viewModelScope.launch {
            val result = cartUseCases.applyDiscountCode(code)
            if (result.isSuccess) {
                couponUseCases.clearCoupon()
                _state.update {
                    it.copy(
                        isApplyingPromo = false,
                        promoError = null,
                        promoCode = ""
                    )
                }
                loadCart()

            } else {
                _state.update {
                    it.copy(
                        isApplyingPromo = false,
                        promoError = result.exceptionOrNull()?.message
                            ?: context.getString(R.string.invalid_promo_code)
                    )
                }
            }
        }
    }

    fun removePromoCode(codeToRemove: String) {
        _state.update { it.copy(isApplyingPromo = true) }
        viewModelScope.launch {
            cartUseCases.removeDiscountCode(codeToRemove)
            loadCart()
            _state.update { it.copy(isApplyingPromo = false) }
        }
    }

    private suspend fun loadSavedCoupon() {
        val couponCode = couponUseCases.getSavedCoupon().firstOrNull()

        if (!couponCode.isNullOrBlank()) {
            _state.update {
                it.copy(
                    promoCode = couponCode
                )
            }
        }
    }
}