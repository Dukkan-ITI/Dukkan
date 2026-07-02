package com.msayeh.domain.usecase.settings

import com.msayeh.domain.repository.SettingsRepository
import javax.inject.Inject

class SetOnboardingStatusUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(completed: Boolean) = repository.setOnboardingCompleted(completed)
}
