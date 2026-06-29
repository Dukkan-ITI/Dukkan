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

sealed interface RegisterAction {
    data class NameChanged(val name: String) : RegisterAction
    data class EmailChanged(val email: String) : RegisterAction
    data class PasswordChanged(val password: String) : RegisterAction
    data object RegisterClicked : RegisterAction
    data object GoogleClicked : RegisterAction
    data class GoogleIdTokenReceived(val idToken: String) : RegisterAction
    data class GoogleSignInFailed(val message: String) : RegisterAction
}

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

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.NameChanged -> {
                _uiState.updateForm { copy(name = action.name, nameError = null) }
            }
            is RegisterAction.EmailChanged -> {
                _uiState.updateForm { copy(email = action.email, emailError = null) }
            }
            is RegisterAction.PasswordChanged -> {
                _uiState.updateForm { copy(password = action.password, passwordError = null) }
            }
            RegisterAction.RegisterClicked -> {
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
            RegisterAction.GoogleClicked -> {
                viewModelScope.launch {
                    _events.send(RegisterEvent.TriggerGoogleSignIn)
                }
            }
            is RegisterAction.GoogleIdTokenReceived -> {
                viewModelScope.launch {
                    _uiState.updateForm { copy(isGoogleLoading = true) }
                    val result = loginWithGoogleUseCase(action.idToken)
                    _uiState.value = if (result.isSuccess) {
                        RegisterUiState.Success
                    } else {
                        RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Google sign-in failed")
                    }
                }
            }
            is RegisterAction.GoogleSignInFailed -> {
                _uiState.updateForm { copy(isGoogleLoading = false) }
                _uiState.value = RegisterUiState.Error(action.message)
            }
        }
    }
}

private fun MutableStateFlow<RegisterUiState>.updateForm(
    block: RegisterUiState.Form.() -> RegisterUiState.Form,
) {
    update { current -> if (current is RegisterUiState.Form) current.block() else current }
}