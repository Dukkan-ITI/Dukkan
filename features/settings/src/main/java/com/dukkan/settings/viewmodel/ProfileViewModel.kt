package com.dukkan.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.design_system.components.OrderStatus
import com.example.design_system.components.OrderUi
import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.usecase.GetCurrentUserUseCase
import com.msayeh.domain.usecase.SignOutUseCase
import com.msayeh.domain.usecase.favorite.GetFavoritesUseCase
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

val sampleOrders: List<OrderUi> = listOf(
    OrderUi(
        id = "#JN-4821",
        date = "Jun 12, 2026",
        itemCount = 3,
        total = "$168",
        status = OrderStatus.DELIVERED,
    ),
    OrderUi(
        id = "#JN-4790",
        date = "May 28, 2026",
        itemCount = 1,
        total = "$95",
        status = OrderStatus.DELIVERED,
    ),
    OrderUi(
        id = "#JN-4763",
        date = "May 09, 2026",
        itemCount = 2,
        total = "$120",
        status = OrderStatus.IN_TRANSIT,
    ),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    getThemeUseCase: GetThemeUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getLanguageUseCase: GetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _user = MutableStateFlow<AuthUser?>(null)
    private val _isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            _user.value = getCurrentUserUseCase()
            _isLoading.value = false
        }
    }

    private val settings = combine(
        getThemeUseCase(),
        getCurrencyUseCase(),
        getLanguageUseCase()
    ) { theme, currency, language ->
        Triple(theme, currency, language)
    }

    val state: StateFlow<ProfileState> = combine(
        _user,
        _isLoading,
        getFavoritesUseCase().map { it.size },
        settings,
    ) { user, isLoading, favoritesCount, (theme, currency, language) ->
        ProfileState(
            isLoading = isLoading,
            user = user,
            favoritesCount = favoritesCount,
            themeMode = theme,
            currency = currency,
            language = language,
            orders = sampleOrders,
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
        // Only persist here. The app root observes the language flow and applies the
        // per-app locale, so the change is picked up both now and on the next launch.
        viewModelScope.launch { setLanguageUseCase(language) }
    }

    fun onLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            _user.value = null
            onComplete()
        }
    }
}
