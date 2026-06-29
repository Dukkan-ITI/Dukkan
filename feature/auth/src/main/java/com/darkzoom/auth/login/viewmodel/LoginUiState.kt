package com.darkzoom.auth.login.viewmodel

import androidx.compose.runtime.Immutable

sealed interface LoginUiState {

    @Immutable
    data class Form(
        val email: String = "",
        val password: String = "",
        val emailError: String? = null,
        val passwordError: String? = null,
    ) : LoginUiState {
        val isSubmitEnabled: Boolean
            get() = email.isNotBlank() && password.length >= 6
    }

    data object Loading : LoginUiState

    data object Success : LoginUiState

    @Immutable
    data class Error(val message: String) : LoginUiState
}