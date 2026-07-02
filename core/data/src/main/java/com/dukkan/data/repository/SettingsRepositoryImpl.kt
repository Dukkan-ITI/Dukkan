package com.dukkan.data.repository

import com.dukkan.data.source.local.SettingsStore
import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val store: SettingsStore) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = store.themeMode.map { value ->
        value?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    override val currency: Flow<AppCurrency> = store.currency.map { value ->
        value?.let { runCatching { AppCurrency.valueOf(it) }.getOrNull() } ?: AppCurrency.USD
    }

    override val language: Flow<AppLanguage> = store.language.map { value ->
        value?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.ENGLISH
    }

    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode.name)

    override suspend fun setCurrency(currency: AppCurrency) = store.setCurrency(currency.name)

    override suspend fun setLanguage(language: AppLanguage) = store.setLanguage(language.name)
}
