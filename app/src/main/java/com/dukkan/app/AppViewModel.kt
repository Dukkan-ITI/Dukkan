package com.dukkan.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.usecase.settings.GetLanguageUseCase
import com.msayeh.domain.usecase.settings.GetThemeUseCase
import com.msayeh.domain.usecase.settings.GetOnboardingStatusUseCase
import com.msayeh.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.dukkan.navigation.Screen
import javax.inject.Inject

/**
 * App-root ViewModel exposing the persisted theme and language preferences so
 * [MainActivity] can drive the app-wide light/dark appearance and per-app locale.
 * Also determines the startDestination based on onboarding and login status.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    getTheme: GetThemeUseCase,
    getLanguage: GetLanguageUseCase,
    getOnboardingStatus: GetOnboardingStatusUseCase,
    getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode?> = getTheme().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val language: StateFlow<AppLanguage?> = getLanguage().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val startDestination: StateFlow<Any?> = combine(
        getOnboardingStatus(),
        kotlinx.coroutines.flow.flow { emit(getCurrentUser()) }
    ) { isOnboardingCompleted, user ->
        when {
            user != null -> Screen.Home
            isOnboardingCompleted -> Screen.Auth
            else -> Screen.Onboarding
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )
}
