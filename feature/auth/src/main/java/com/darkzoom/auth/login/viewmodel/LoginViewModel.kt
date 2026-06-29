package com.darkzoom.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.usecase.GetCurrentUserUseCase
import com.msayeh.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

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
            val result = loginUseCase(form.email, form.password)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
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