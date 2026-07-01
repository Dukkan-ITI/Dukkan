package com.dukkan.auth.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {
    @Test
    fun `login form is invalid for empty email`() {
        val viewModel = AuthViewModel()

        viewModel.onAction(AuthAction.EmailChanged(""))
        viewModel.onAction(AuthAction.PasswordChanged("123456"))

        val state = viewModel.uiState.value as? AuthUiState.Form
        assertFalse(state?.isSubmitEnabled == true)
    }

    @Test
    fun `register form becomes enabled with full details`() {
        val viewModel = AuthViewModel()

        viewModel.onAction(AuthAction.ToggleMode)
        viewModel.onAction(AuthAction.NameChanged("Ada"))
        viewModel.onAction(AuthAction.EmailChanged("ada@example.com"))
        viewModel.onAction(AuthAction.PasswordChanged("123456"))

        val state = viewModel.uiState.value as? AuthUiState.Form
        assertTrue(state?.isSubmitEnabled == true)
        assertTrue(state?.isLoginMode == false)
    }
}
