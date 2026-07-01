package com.dukkan.auth.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msayeh.domain.usecase.GetShopifyTokenUseCase
import com.msayeh.domain.usecase.LoginUseCase
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

sealed interface AuthAction {
    data class NameChanged(val name: String) : AuthAction
    data class EmailChanged(val email: String) : AuthAction
    data class PasswordChanged(val password: String) : AuthAction
    data object ToggleMode : AuthAction
    data object SubmitClicked : AuthAction
    data object GoogleClicked : AuthAction
    data class GoogleIdTokenReceived(val idToken: String) : AuthAction
    data class GoogleSignInFailed(val message: String) : AuthAction
}

sealed interface AuthEvent {
    data object TriggerGoogleSignIn : AuthEvent
}

sealed interface AuthUiState {
    @Immutable
    data class Form(
        val isLoginMode: Boolean = true,
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val nameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val isGoogleLoading: Boolean = false,
    ) : AuthUiState {
        val isSubmitEnabled: Boolean
            get() = if (isLoginMode) {
                email.isNotBlank() && password.length >= 6
            } else {
                name.isNotBlank() && email.isNotBlank() && password.length >= 6
            }
    }

    data object Loading : AuthUiState

    data object Success : AuthUiState

    @Immutable
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getShopifyTokenUseCase: GetShopifyTokenUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Form())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: AuthAction) = when (action) {
        is AuthAction.NameChanged -> onNameChanged(action.name)
        is AuthAction.EmailChanged -> onEmailChanged(action.email)
        is AuthAction.PasswordChanged -> onPasswordChanged(action.password)
        AuthAction.ToggleMode -> toggleMode()
        AuthAction.SubmitClicked -> submit()
        AuthAction.GoogleClicked -> triggerGoogleSignIn()
        is AuthAction.GoogleIdTokenReceived -> loginWithGoogle(action.idToken)
        is AuthAction.GoogleSignInFailed -> onGoogleFailure(action.message)
    }

    private fun onNameChanged(name: String) {
        _uiState.updateForm { copy(name = name, nameError = null) }
    }

    private fun onEmailChanged(email: String) {
        _uiState.updateForm { copy(email = email, emailError = null) }
    }

    private fun onPasswordChanged(password: String) {
        _uiState.updateForm { copy(password = password, passwordError = null) }
    }

    private fun toggleMode() {
        _uiState.updateForm { copy(isLoginMode = !isLoginMode) }
    }

    private fun submit() {
        val form = _uiState.value as? AuthUiState.Form ?: return
        if (!form.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = if (form.isLoginMode) {
                loginUseCase(form.email, form.password)
            } else {
                registerUseCase(form.email, form.password)
            }

            if (result.isSuccess == true) {
                // Best-effort: fetch Shopify token so it is available app-wide
                getShopifyTokenUseCase()
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private fun triggerGoogleSignIn() {
        viewModelScope.launch {
            _events.send(AuthEvent.TriggerGoogleSignIn)
        }
    }

    private fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.updateForm { copy(isGoogleLoading = true) }
            val result = loginWithGoogleUseCase(idToken)
            _uiState.value = if (result.isSuccess == true) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Google sign-in failed")
            }
        }
    }

    private fun onGoogleFailure(message: String) {
        _uiState.updateForm { copy(isGoogleLoading = false) }
        _uiState.value = AuthUiState.Error(message)
    }
}

private fun MutableStateFlow<AuthUiState>.updateForm(
    block: AuthUiState.Form.() -> AuthUiState.Form,
) {
    update { current -> if (current is AuthUiState.Form) current.block() else current }
}
