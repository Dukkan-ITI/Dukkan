package com.dukkan.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.onboarding.uistate.OnboardingUiState
import com.dukkan.onboarding.R
import com.dukkan.onboarding.model.OnboardingModel
import com.dukkan.domain.usecase.settings.SetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            pages = listOf(
                OnboardingModel(
                    R.string.onboarding_title_1,
                    R.string.onboarding_desc_1,
                    R.drawable.onboarding1,
                ),
                OnboardingModel(
                    R.string.onboarding_title_2,
                    R.string.onboarding_desc_2,
                    R.drawable.onboarding2,
                ),
                OnboardingModel(
                    R.string.onboarding_title_3,
                    R.string.onboarding_desc_3,
                    R.drawable.onboarding3,
                ),
            )
        )
    )

    val uiState = _uiState.asStateFlow()

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            setOnboardingStatusUseCase(true)
            onComplete()
        }
    }
}
