package com.dukkan.auth.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.usecase.GetShopifyTokenUseCase
import com.dukkan.domain.usecase.LoginUseCase
import com.dukkan.domain.usecase.LoginWithGoogleUseCase
import com.dukkan.domain.usecase.RegisterUseCase
import com.dukkan.domain.usecase.cart.SyncCartOnLoginUseCase
import com.dukkan.domain.usecase.favorite.SyncFavoritesOnLoginUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    data object ResendVerificationClicked : AuthAction
    data object CheckVerificationClicked : AuthAction
    data object BackToLoginClicked : AuthAction
    data object ForgotPasswordClicked : AuthAction
    data class ForgotPasswordEmailChanged(val email: String) : AuthAction
    data object SendResetLinkClicked : AuthAction
    data object BackToLoginFromForgotPasswordClicked : AuthAction
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

    @Immutable
    data class EmailVerificationPending(
        val email: String,
        val isResending: Boolean = false,
        val isChecking: Boolean = false,
        val resendCooldownSeconds: Int = 0,
        val infoMessage: String? = null,
    ) : AuthUiState

    @Immutable
    data class ForgotPassword(
        val email: String = "",
        val isLoading: Boolean = false,
        val emailError: String? = null,
        val isEmailSent: Boolean = false,
    ) : AuthUiState {
        val isSubmitEnabled: Boolean
            get() = email.isNotBlank()
    }

    data object Success : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getShopifyTokenUseCase: GetShopifyTokenUseCase,
    private val syncFavoritesOnLoginUseCase: SyncFavoritesOnLoginUseCase,
    private val syncCartOnLoginUseCase: SyncCartOnLoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Form())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var cooldownJob: Job? = null

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
        AuthAction.ResendVerificationClicked -> resendVerificationEmail()
        AuthAction.CheckVerificationClicked -> checkEmailVerified()
        AuthAction.BackToLoginClicked -> backToLogin()
        AuthAction.ForgotPasswordClicked -> openForgotPassword()
        is AuthAction.ForgotPasswordEmailChanged -> onForgotPasswordEmailChanged(action.email)
        AuthAction.SendResetLinkClicked -> sendResetLink()
        AuthAction.BackToLoginFromForgotPasswordClicked -> backToLoginFromForgotPassword()
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
                handlePostEmailAuthSuccess(isNewRegistration = !form.isLoginMode)
            } else {
                _uiState.value = form.copy(
                    emailError = result.exceptionOrNull()?.message
                        ?: context.getString(com.dukkan.auth.R.string.auth_error_unknown)
                )
            }
        }
    }

    private suspend fun handlePostEmailAuthSuccess(isNewRegistration: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            _uiState.value = AuthUiState.Form()
            return
        }

        try {
            user.reload().await()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to reload user", e)
        }

        val refreshedUser = FirebaseAuth.getInstance().currentUser

        if (refreshedUser?.isEmailVerified == true) {
            viewModelScope.launch {
                getShopifyTokenUseCase()
                syncFavoritesAfterAuth()
                syncCartAfterAuth()
            }
            _uiState.value = AuthUiState.Success
            return
        }

        if (isNewRegistration) {
            try {
                refreshedUser?.sendEmailVerification()?.await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to send verification email", e)
            }
            _uiState.value =
                AuthUiState.EmailVerificationPending(email = refreshedUser?.email.orEmpty())
            startResendCooldown()
        } else {
            _uiState.value =
                AuthUiState.EmailVerificationPending(email = refreshedUser?.email.orEmpty())
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (seconds in 60 downTo 0) {
                val state = _uiState.value as? AuthUiState.EmailVerificationPending ?: return@launch
                _uiState.value = state.copy(resendCooldownSeconds = seconds)
                if (seconds > 0) delay(1000)
            }
        }
    }

    private fun resendVerificationEmail() {
        val state = _uiState.value as? AuthUiState.EmailVerificationPending ?: return
        if (state.resendCooldownSeconds > 0) return
        viewModelScope.launch {
            _uiState.value = state.copy(isResending = true, infoMessage = null)
            try {
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.await()
                _uiState.value = state.copy(
                    isResending = false,
                    infoMessage = context.getString(com.dukkan.auth.R.string.auth_verification_email_resent)
                )
                startResendCooldown()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isResending = false,
                    infoMessage = e.message
                        ?: context.getString(com.dukkan.auth.R.string.auth_error_unknown)
                )
            }
        }
    }

    private fun checkEmailVerified() {
        val state = _uiState.value as? AuthUiState.EmailVerificationPending ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isChecking = true, infoMessage = null)
            val user = FirebaseAuth.getInstance().currentUser
            try {
                user?.reload()?.await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to reload user", e)
            }

            val refreshedUser = FirebaseAuth.getInstance().currentUser
            if (refreshedUser?.isEmailVerified == true) {
                viewModelScope.launch {
                    getShopifyTokenUseCase()
                    syncFavoritesAfterAuth()
                    syncCartAfterAuth()
                }
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = state.copy(
                    isChecking = false,
                    infoMessage = context.getString(com.dukkan.auth.R.string.auth_verification_still_pending)
                )
            }
        }
    }

    private fun backToLogin() {
        cooldownJob?.cancel()
        FirebaseAuth.getInstance().signOut()
        _uiState.value = AuthUiState.Form(isLoginMode = true)
    }

    private fun openForgotPassword() {
        val currentEmail = (_uiState.value as? AuthUiState.Form)?.email.orEmpty()
        _uiState.value = AuthUiState.ForgotPassword(email = currentEmail)
    }

    private fun onForgotPasswordEmailChanged(email: String) {
        val state = _uiState.value as? AuthUiState.ForgotPassword ?: return
        _uiState.value = state.copy(email = email, emailError = null)
    }

    private fun sendResetLink() {
        val state = _uiState.value as? AuthUiState.ForgotPassword ?: return
        if (!state.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, emailError = null)
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(state.email).await()
                _uiState.value = state.copy(isLoading = false, isEmailSent = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    emailError = e.message
                        ?: context.getString(com.dukkan.auth.R.string.auth_error_unknown)
                )
            }
        }
    }

    private fun backToLoginFromForgotPassword() {
        val previousEmail = (_uiState.value as? AuthUiState.ForgotPassword)?.email.orEmpty()
        _uiState.value = AuthUiState.Form(isLoginMode = true, email = previousEmail)
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
                syncFavoritesAfterAuth()
                syncCartAfterAuth()

                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = form.copy(
                    isGoogleLoading = false,
                    emailError = result.exceptionOrNull()?.message
                        ?: context.getString(com.dukkan.auth.R.string.auth_error_google_sign_in_failed)
                )
            }
        }
    }

    suspend fun syncFavoritesAfterAuth() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            syncFavoritesOnLoginUseCase(userId)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to sync favorites", e)
        }
    }

    suspend fun syncCartAfterAuth() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            syncCartOnLoginUseCase(userId)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to sync cart", e)
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