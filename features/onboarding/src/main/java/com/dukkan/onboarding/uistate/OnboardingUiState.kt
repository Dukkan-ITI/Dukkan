package com.dukkan.onboarding.uistate

import com.dukkan.onboarding.model.OnboardingModel

data class OnboardingUiState(
    val pages: List<OnboardingModel> = emptyList()
)