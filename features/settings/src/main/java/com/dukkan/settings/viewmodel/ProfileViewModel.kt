package com.dukkan.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.design_system.components.OrderUi
import com.dukkan.settings.mapper.toOrderUi
import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.usecase.GetCurrentUserUseCase
import com.msayeh.domain.usecase.SignOutUseCase
import com.msayeh.domain.usecase.favorite.GetFavoritesUseCase
import com.msayeh.domain.usecase.order.GetRecentOrdersUseCase
import com.msayeh.domain.usecase.settings.GetCurrencyUseCase
import com.msayeh.domain.usecase.settings.GetLanguageUseCase
import com.msayeh.domain.usecase.settings.GetThemeUseCase
import com.msayeh.domain.usecase.settings.SetCurrencyUseCase
import com.msayeh.domain.usecase.settings.SetLanguageUseCase
import com.msayeh.domain.usecase.settings.SetThemeUseCase
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
            signOutUseCase()
            _user.value = null
            _orders.value = emptyList()
            onComplete()
        }
    }
}
