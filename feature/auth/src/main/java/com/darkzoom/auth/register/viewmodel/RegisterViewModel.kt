package com.darkzoom.auth.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Form())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.updateForm { copy(name = name, nameError = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.updateForm { copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.updateForm { copy(password = password, passwordError = null) }
    }

    fun onRegisterClicked() {
        val form = _uiState.value as? RegisterUiState.Form ?: return
        if (!form.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = registerUseCase(form.email, form.password)
            if (result.isSuccess) {
                _uiState.value = RegisterUiState.Success
            } else {
                _uiState.value = RegisterUiState.Error(
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

private fun MutableStateFlow<RegisterUiState>.updateForm(
    block: RegisterUiState.Form.() -> RegisterUiState.Form,
) {
    update { current -> if (current is RegisterUiState.Form) current.block() else current }
}