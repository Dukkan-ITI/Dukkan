package com.msayeh.domain.usecase.settings

import com.msayeh.domain.model.ThemeMode
import com.msayeh.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.themeMode
}

class SetThemeUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
