package com.dukkan.domain.usecase.settings

import com.dukkan.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isOnboardingCompleted
}
