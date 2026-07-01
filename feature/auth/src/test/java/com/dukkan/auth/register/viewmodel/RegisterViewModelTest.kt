package com.dukkan.auth.viewmodel

import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {
    @Test
    fun `toggle mode switches from login to register`() {
        val viewModel = AuthViewModel()

        assertTrue((viewModel.uiState.value as AuthUiState.Form).isLoginMode)

        viewModel.onAction(AuthAction.ToggleMode)

        val state = viewModel.uiState.value as? AuthUiState.Form
        assertTrue(state?.isLoginMode == false)
    }
}
