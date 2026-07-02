package com.msayeh.domain.repository

import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val currency: Flow<AppCurrency>
    val language: Flow<AppLanguage>
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrency(currency: AppCurrency)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
