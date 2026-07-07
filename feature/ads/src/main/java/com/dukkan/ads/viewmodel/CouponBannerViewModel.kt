package com.dukkan.ads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.ads.uistate.CouponBannerUIState
import com.dukkan.domain.model.Coupon
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.coupon.CouponUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponBannerViewModel @Inject constructor(
    private val couponUseCases: CouponUseCases,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow((CouponBannerUIState()))
    val state: StateFlow<CouponBannerUIState> = _state.asStateFlow()

    init {
        _state.update { it.copy(coupons = couponUseCases.getAvailableCoupons()) }
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val user = getCurrentUser()
            _state.update { it.copy(isLoggedIn = user != null) }
        }
    }

    fun onCouponClick(coupon: Coupon) {
        if (!_state.value.isLoggedIn) {
            _state.update { it.copy(showGuestDialog = true) }
            return
        }

        viewModelScope.launch {
            couponUseCases.saveCoupon(coupon.code)
            _state.update { it.copy(savedSuccess = true) }
            delay(2000)
            _state.update { it.copy(savedSuccess = false) }
        }
    }

    fun dismissGuestDialog() {
        _state.update { it.copy(showGuestDialog = false) }
    }
}
