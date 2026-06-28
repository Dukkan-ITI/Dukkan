package com.dukkan.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import com.dukkan.onboarding.uistate.OnboardingUiState
import com.dukkan.onboarding.R
import com.dukkan.onboarding.model.OnboardingModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            pages = listOf(
                OnboardingModel(
                    "Welcome to Dukkan",
                    "Discover the best products from top brands tailored just for you.",
                    R.drawable.onboarding1
                ),
                OnboardingModel(
                    "Shop Safely",
                    "Your security is our priority. Shop with confidence using our secure payment options.",
                    R.drawable.onboarding2
                ),
                OnboardingModel(
                    "Fast Delivery",
                    "Get your favorite items delivered right to your doorstep in no time.",
                    R.drawable.onboarding3
                )
            )
        )
    )

    val uiState = _uiState.asStateFlow()
}