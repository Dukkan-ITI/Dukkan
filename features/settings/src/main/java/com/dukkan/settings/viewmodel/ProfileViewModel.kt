package com.dukkan.settings.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.design_system.components.OrderUi
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.ThemeMode
import com.dukkan.domain.usecase.GetCurrentUserUseCase
import com.dukkan.domain.usecase.SignOutUseCase
import com.dukkan.domain.usecase.cart.ClearCartOnLogoutUseCase
import com.dukkan.domain.usecase.favorite.ClearFavoritesOnLogoutUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import com.dukkan.domain.usecase.settings.GetLanguageUseCase
import com.dukkan.domain.usecase.settings.GetThemeUseCase
import com.dukkan.domain.usecase.settings.SetCurrencyUseCase
import com.dukkan.domain.usecase.settings.SetLanguageUseCase
import com.dukkan.domain.usecase.settings.SetThemeUseCase
import com.dukkan.settings.mapper.toOrderUi
import com.dukkan.domain.usecase.order.GetRecentOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    getThemeUseCase: GetThemeUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getLanguageUseCase: GetLanguageUseCase,
    private val getRecentOrdersUseCase: GetRecentOrdersUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val clearFavoritesOnLogoutUseCase: ClearFavoritesOnLogoutUseCase,
    private val clearCartOnLogoutUseCase: ClearCartOnLogoutUseCase,


    ) : ViewModel() {

    private val _user = MutableStateFlow<AuthUser?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _orders = MutableStateFlow<List<OrderUi>>(emptyList())
    private val _ordersLoading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _user.value = user
            _isLoading.value = false
            if (user != null) {
                loadRecentOrders()
            }
        }
    }

    private fun loadRecentOrders() {
        viewModelScope.launch {
            _ordersLoading.value = true
            getRecentOrdersUseCase()
                .onSuccess { orders -> _orders.value = orders.map { it.toOrderUi() } }
                .onFailure { _orders.value = emptyList() }
            _ordersLoading.value = false
        }
    }

    private val settings = combine(
        getThemeUseCase(),
        getCurrencyUseCase(),
        getLanguageUseCase()
    ) { theme, currency, language ->
        Triple(theme, currency, language)
    }

    private val ordersState = combine(_orders, _ordersLoading) { orders, ordersLoading ->
        orders to ordersLoading
    }

    val state: StateFlow<ProfileState> = combine(
        _user,
        _isLoading,
        ordersState,
        getFavoritesUseCase().map { it.size },
        settings,
    ) { user, isLoading, ordersData, favoritesCount, settingsTriple ->
        val (orders, ordersLoading) = ordersData
        val (theme, currency, language) = settingsTriple
        ProfileState(
            isLoading = isLoading,
            user = user,
            favoritesCount = favoritesCount,
            themeMode = theme,
            currency = currency,
            language = language,
            orders = orders,
            ordersLoading = ordersLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileState(),
    )

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { setThemeUseCase(mode) }
    }

    fun onCurrencySelected(currency: AppCurrency) {
        viewModelScope.launch { setCurrencyUseCase(currency) }
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { setLanguageUseCase(language) }
    }

    fun onLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = _user.value?.uid

            signOutUseCase()

            if (userId != null) {
                try {
                    clearFavoritesOnLogoutUseCase(userId)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Failed to clear favorites on logout", e)
                }
            }

            try {
                clearCartOnLogoutUseCase()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to clear cart on logout", e)
            }

            _user.value = null
            _orders.value = emptyList()
            onComplete()
        }
    }
}
