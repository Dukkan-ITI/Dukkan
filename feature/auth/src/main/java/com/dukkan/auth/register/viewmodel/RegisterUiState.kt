package com.dukkan.auth.register.viewmodel

import androidx.compose.runtime.Immutable

sealed interface RegisterUiState {

    @Immutable
    data class Form(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val nameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val isGoogleLoading: Boolean = false,
    ) : RegisterUiState {
        val isSubmitEnabled: Boolean
            get() = name.isNotBlank()
                    && email.isNotBlank()
                    && password.length >= 6
    }

    data object Loading : RegisterUiState

    data object Success : RegisterUiState

    @Immutable
    data class Error(val message: String) : RegisterUiState
}