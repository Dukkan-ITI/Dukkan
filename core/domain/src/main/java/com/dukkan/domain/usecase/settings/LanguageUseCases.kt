package com.dukkan.domain.usecase.settings

import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppLanguage> = repository.language
}

class SetLanguageUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
