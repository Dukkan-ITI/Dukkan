package com.darkzoom.auth.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.usecase.LoginWithGoogleUseCase
import com.msayeh.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RegisterEvent {
    data object TriggerGoogleSignIn : RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Form())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
            _uiState.value = if (result.isSuccess) {
                RegisterUiState.Success
            } else {
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun onGoogleClicked() {
        viewModelScope.launch {
            _events.send(RegisterEvent.TriggerGoogleSignIn)
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _uiState.updateForm { copy(isGoogleLoading = true) }
            val result = loginWithGoogleUseCase(idToken)
            _uiState.value = if (result.isSuccess) {
                RegisterUiState.Success
            } else {
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Google sign-in failed")
            }
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.updateForm { copy(isGoogleLoading = false) }
        _uiState.value = RegisterUiState.Error(message)
    }
}

private fun MutableStateFlow<RegisterUiState>.updateForm(
    block: RegisterUiState.Form.() -> RegisterUiState.Form,
) {
    update { current -> if (current is RegisterUiState.Form) current.block() else current }
}