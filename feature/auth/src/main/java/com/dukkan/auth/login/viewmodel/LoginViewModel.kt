package com.dukkan.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.usecase.GetCurrentUserUseCase
import com.msayeh.domain.usecase.LoginUseCase
import com.msayeh.domain.usecase.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
    data object GoogleClicked : LoginAction
    data class GoogleIdTokenReceived(val idToken: String) : LoginAction
    data class GoogleSignInFailed(val message: String) : LoginAction
}

sealed interface LoginEvent {
    data object TriggerGoogleSignIn : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Form())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                _uiState.updateForm { copy(email = action.email, emailError = null) }
            }
            is LoginAction.PasswordChanged -> {
                _uiState.updateForm { copy(password = action.password, passwordError = null) }
            }
            LoginAction.LoginClicked -> {
                val form = _uiState.value as? LoginUiState.Form ?: return
                if (!form.isSubmitEnabled) return

                viewModelScope.launch {
                    _uiState.value = LoginUiState.Loading
                    val result = loginUseCase(form.email, form.password)
                    _uiState.value = if (result.isSuccess) {
                        LoginUiState.Success
                    } else {
                        LoginUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                    }
                }
            }
            LoginAction.GoogleClicked -> {
                viewModelScope.launch {
                    _events.send(LoginEvent.TriggerGoogleSignIn)
                }
            }
            is LoginAction.GoogleIdTokenReceived -> {
                viewModelScope.launch {
                    _uiState.updateForm { copy(isGoogleLoading = true) }
                    val result = loginWithGoogleUseCase(action.idToken)
                    _uiState.value = if (result.isSuccess) {
                        LoginUiState.Success
                    } else {
                        LoginUiState.Error(result.exceptionOrNull()?.message ?: "Google sign-in failed")
                    }
                }
            }
            is LoginAction.GoogleSignInFailed -> {
                _uiState.updateForm { copy(isGoogleLoading = false) }
                _uiState.value = LoginUiState.Error(action.message)
            }
        }
    }
}

private fun MutableStateFlow<LoginUiState>.updateForm(
    block: LoginUiState.Form.() -> LoginUiState.Form,
) {
    update { current -> if (current is LoginUiState.Form) current.block() else current }
}