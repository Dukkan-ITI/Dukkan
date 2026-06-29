package com.darkzoom.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Form())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.updateForm { copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.updateForm { copy(password = password, passwordError = null) }
    }

    fun onLoginClicked() {
        val form = _uiState.value as? LoginUiState.Form ?: return
        if (!form.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

        }
    }

    fun onGoogleClicked() {
    }

    fun onAppleClicked() {
    }
}

private fun MutableStateFlow<LoginUiState>.updateForm(
    block: LoginUiState.Form.() -> LoginUiState.Form,
) {
    update { current -> if (current is LoginUiState.Form) current.block() else current }
}