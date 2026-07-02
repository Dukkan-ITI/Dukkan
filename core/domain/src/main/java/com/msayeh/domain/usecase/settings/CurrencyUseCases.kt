package com.msayeh.domain.usecase.settings

import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencyUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppCurrency> = repository.currency
}

class SetCurrencyUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(currency: AppCurrency) = repository.setCurrency(currency)
}
