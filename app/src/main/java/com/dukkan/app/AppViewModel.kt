package com.dukkan.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.usecase.settings.GetLanguageUseCase
import com.msayeh.domain.usecase.settings.GetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * App-root ViewModel exposing the persisted theme and language preferences so
 * [MainActivity] can drive the app-wide light/dark appearance and per-app locale.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    getTheme: GetThemeUseCase,
    getLanguage: GetLanguageUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = getTheme().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM,
    )

    val language: StateFlow<AppLanguage> = getLanguage().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppLanguage.ENGLISH,
    )
}
