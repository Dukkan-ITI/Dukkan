package com.dukkan.onboarding.model

import androidx.annotation.StringRes

data class OnboardingModel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val imageRes: Int,
)
