package com.dukkan.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

/**
 * Persists user app preferences (theme, currency, language) as raw enum names.
 * Emits null when a preference has never been set so the repository can apply defaults.
 */
interface SettingsStore {
    val themeMode: Flow<String?>
    val currency: Flow<String?>
    val language: Flow<String?>
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setThemeMode(value: String)
    suspend fun setCurrency(value: String)
    suspend fun setLanguage(value: String)
    suspend fun setOnboardingCompleted(value: Boolean)
}

class SettingsStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsStore {

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_CURRENCY = stringPreferencesKey("currency")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val themeMode: Flow<String?> =
        context.appSettingsDataStore.data.map { it[KEY_THEME_MODE] }

    override val currency: Flow<String?> =
        context.appSettingsDataStore.data.map { it[KEY_CURRENCY] }

    override val language: Flow<String?> =
        context.appSettingsDataStore.data.map { it[KEY_LANGUAGE] }

    override val isOnboardingCompleted: Flow<Boolean> =
        context.appSettingsDataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    override suspend fun setThemeMode(value: String) {
        context.appSettingsDataStore.edit { it[KEY_THEME_MODE] = value }
    }

    override suspend fun setCurrency(value: String) {
        context.appSettingsDataStore.edit { it[KEY_CURRENCY] = value }
    }

    override suspend fun setLanguage(value: String) {
        context.appSettingsDataStore.edit { it[KEY_LANGUAGE] = value }
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        context.appSettingsDataStore.edit { it[KEY_ONBOARDING_COMPLETED] = value }
    }
}
