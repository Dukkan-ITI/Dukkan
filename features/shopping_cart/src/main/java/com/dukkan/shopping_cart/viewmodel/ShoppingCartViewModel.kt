package com.dukkan.shopping_cart.viewmodel

import android.content.Context
import android.icu.number.Precision.currency
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.shopping_cart.R
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import dagger.hilt.android.qualifiers.ApplicationContext
import com.dukkan.domain.model.cart.CartLine
import com.dukkan.domain.repository.SettingsRepository
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.cart.CartUseCases
import com.dukkan.domain.usecase.coupon.CouponUseCases
import com.dukkan.domain.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(
    private val cartUseCases: CartUseCases,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val couponUseCases: CouponUseCases,
    private val settingsRepository: SettingsRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _state = MutableStateFlow(ShoppingCartState())
    val state: StateFlow<ShoppingCartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _state.update { it.copy(isOnline = isOnline) }
                if (isOnline && _isLoggedIn.value) {
                    refreshCart()
                }
            }
        }

        viewModelScope.launch {
            val user = getCurrentUser()
            _isLoggedIn.value = user != null

            if (user != null) {
                loadCart()
            }
        }

        viewModelScope.launch {
            settingsRepository.currency
                .drop(1)
                .collectLatest {
                    Log.d("Currency", "Changed to ${it.countryCode}, settingsRepository.currency}")
                    if (_isLoggedIn.value) {
                        refreshCart()
                    }
                }
        }

    }

    private suspend fun loadCart() {
        if (_state.value.cart == null) {
            _state.update { it.copy(isLoading = true) }
        }

        val cart = cartUseCases.getCart()

        _state.update {
            it.copy(
                cart = cart,
                isLoading = false
            )
        }
    }

    private suspend fun refreshCart() {
        Log.d("Cart", "Refreshing cart")
        val cart = cartUseCases.getCart()
        _state.update { it.copy(cart = cart) }
    }

    fun onScreenEntered() {
        viewModelScope.launch {
            refreshCart()
            loadSavedCoupon()
        }
    }

    fun updateQuantity(cartLine: CartLine, newQuantity: Int) {
        if (!_state.value.isOnline) {
            showOfflineToast()
            return
        }
        viewModelScope.launch {
            _state.update { state ->
                val cart = state.cart ?: return@update state

                val updatedLines = cart.lines.map { line ->
                    if (line.id == cartLine.id) {
                        line.copy(quantity = newQuantity)
                    } else {
                        line
                    }
                }

                state.copy(cart = cart.copy(lines = updatedLines))
            }

            try {
                cartUseCases.updateCartQuantity(cartLine.id, newQuantity)
                refreshCart()
            } catch (_: Exception) {
                refreshCart()
            }
        }
    }

    fun showRemoveDialog(cartLine: CartLine) {
        if (!_state.value.isOnline) {
            showOfflineToast()
            return
        }
        _state.update {
            it.copy(showRemoveDialogForItem = cartLine)
        }
    }

    fun dismissRemoveDialog() {
        _state.update {
            it.copy(showRemoveDialogForItem = null)
        }
    }

    fun confirmRemoveItem() {
        if (!_state.value.isOnline) {
            showOfflineToast()
            return
        }
        _state.value.showRemoveDialogForItem?.let { item ->
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true,
                        showRemoveDialogForItem = null
                    )
                }

                cartUseCases.removeFromCart(item.id)
                loadCart()
            }
        }
    }

    fun onPromoCodeChange(code: String) {
        _state.update {
            it.copy(
                promoCode = code,
                promoError = null
            )
        }
    }

    fun applyPromoCode() {
        if (!_state.value.isOnline) {
            showOfflineToast()
            return
        }
        val code = _state.value.promoCode

        if (code.isBlank()) return

        _state.update {
            it.copy(
                isApplyingPromo = true,
                promoError = null
            )
        }

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
        if (!_state.value.isOnline) {
            showOfflineToast()
            return
        }
        _state.update {
            it.copy(isApplyingPromo = true)
        }

        viewModelScope.launch {
            cartUseCases.removeDiscountCode(codeToRemove)
            loadCart()

            _state.update {
                it.copy(isApplyingPromo = false)
            }
        }
    }

    private fun showOfflineToast() {
        viewModelScope.launch {
            _state.update { it.copy(showOfflineToast = true) }
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(showOfflineToast = false) }
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