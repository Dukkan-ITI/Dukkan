package com.dukkan.auth.login.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    @Test
    fun `login form is invalid for empty email`() {
        val viewModel = LoginViewModel()

        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("123456")

        assertFalse(viewModel.isLoginEnabled())
    }

    @Test
    fun `login form is valid for complete credentials`() {
        val viewModel = LoginViewModel()

        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")

        assertTrue(viewModel.isLoginEnabled())
    }
}
