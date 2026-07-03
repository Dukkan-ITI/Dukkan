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
    data class FirstNameChanged(val firstName: String) : AuthAction
    data class LastNameChanged(val lastName: String) : AuthAction
    data class EmailChanged(val email: String) : AuthAction
    data class PasswordChanged(val password: String) : AuthAction
    data class ConfirmPasswordChanged(val password: String) : AuthAction
    data object TogglePasswordVisibility : AuthAction
    data object ToggleConfirmPasswordVisibility : AuthAction
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
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isGoogleLoading: Boolean = false,
    ) : AuthUiState {
        val isSubmitEnabled: Boolean
            get() = if (isLoginMode) {
                email.isNotBlank() && password.length >= 6
            } else {
                firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.length >= 6 && confirmPassword.length >= 6
            }
    }

    data object Loading : AuthUiState

    data object Success : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
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
        is AuthAction.FirstNameChanged -> onFirstNameChanged(action.firstName)
        is AuthAction.LastNameChanged -> onLastNameChanged(action.lastName)
        is AuthAction.EmailChanged -> onEmailChanged(action.email)
        is AuthAction.PasswordChanged -> onPasswordChanged(action.password)
        is AuthAction.ConfirmPasswordChanged -> onConfirmPasswordChanged(action.password)
        AuthAction.TogglePasswordVisibility -> togglePasswordVisibility()
        AuthAction.ToggleConfirmPasswordVisibility -> toggleConfirmPasswordVisibility()
        AuthAction.ToggleMode -> toggleMode()
        AuthAction.SubmitClicked -> submit()
        AuthAction.GoogleClicked -> triggerGoogleSignIn()
        is AuthAction.GoogleIdTokenReceived -> loginWithGoogle(action.idToken)
        is AuthAction.GoogleSignInFailed -> onGoogleFailure(action.message)
    }

    private fun onFirstNameChanged(firstName: String) {
        _uiState.updateForm { copy(firstName = firstName, firstNameError = null) }
    }
    
    private fun onLastNameChanged(lastName: String) {
        _uiState.updateForm { copy(lastName = lastName, lastNameError = null) }
    }

    private fun onEmailChanged(email: String) {
        _uiState.updateForm { copy(email = email, emailError = null) }
    }

    private fun onPasswordChanged(password: String) {
        _uiState.updateForm { copy(password = password, passwordError = null) }
    }
    
    private fun onConfirmPasswordChanged(password: String) {
        _uiState.updateForm { copy(confirmPassword = password, confirmPasswordError = null) }
    }

    private fun togglePasswordVisibility() {
        _uiState.updateForm { copy(isPasswordVisible = !isPasswordVisible) }
    }
    
    private fun toggleConfirmPasswordVisibility() {
        _uiState.updateForm { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
    }

    private fun toggleMode() {
        _uiState.updateForm { copy(isLoginMode = !isLoginMode) }
    }

    private fun submit() {
        val form = _uiState.value as? AuthUiState.Form ?: return
        if (!form.isSubmitEnabled) return
        
        if (!form.isLoginMode && form.password != form.confirmPassword) {
            _uiState.updateForm { copy(confirmPasswordError = context.getString(com.dukkan.auth.R.string.auth_error_passwords_do_not_match)) }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = if (form.isLoginMode) {
                loginUseCase(form.email, form.password)
            } else {
                registerUseCase(form.email, form.password, form.firstName, form.lastName)
            }

            if (result.isSuccess == true) {
                getShopifyTokenUseCase()
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = form.copy(emailError = result.exceptionOrNull()?.message ?: context.getString(com.dukkan.auth.R.string.auth_error_unknown))
            }
        }
    }

    private fun triggerGoogleSignIn() {
        viewModelScope.launch {
            _events.send(AuthEvent.TriggerGoogleSignIn)
        }
    }

    private fun loginWithGoogle(idToken: String) {
        val form = _uiState.value as? AuthUiState.Form ?: AuthUiState.Form()
        viewModelScope.launch {
            _uiState.value = form.copy(isGoogleLoading = true)
            val result = loginWithGoogleUseCase(idToken)
            if (result.isSuccess == true) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = form.copy(isGoogleLoading = false, emailError = result.exceptionOrNull()?.message ?: context.getString(com.dukkan.auth.R.string.auth_error_google_sign_in_failed))
            }
        }
    }

    private fun onGoogleFailure(message: String) {
        val form = _uiState.value as? AuthUiState.Form ?: AuthUiState.Form()
        _uiState.value = form.copy(isGoogleLoading = false, emailError = message)
    }
}

private fun MutableStateFlow<AuthUiState>.updateForm(
    block: AuthUiState.Form.() -> AuthUiState.Form,
) {
    update { current -> if (current is AuthUiState.Form) current.block() else current }
}
