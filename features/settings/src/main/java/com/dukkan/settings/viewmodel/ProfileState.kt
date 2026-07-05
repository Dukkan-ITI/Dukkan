package com.dukkan.settings.viewmodel

import com.dukkan.design_system.components.OrderUi
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.ThemeMode

data class ProfileState(
    val isLoading: Boolean = true,
    val user: AuthUser? = null,
    val favoritesCount: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: AppCurrency = AppCurrency.USD,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val orders: List<OrderUi> = emptyList(),
    val ordersLoading: Boolean = false,
) {
    val isLoggedIn: Boolean get() = user != null
}
