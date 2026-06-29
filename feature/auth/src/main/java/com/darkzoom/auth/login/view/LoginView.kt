package com.darkzoom.auth.login.view

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darkzoom.auth.R
import com.darkzoom.auth.shared.GoogleSignInHelper
import com.darkzoom.auth.shared.components.AuthErrorScreen
import com.darkzoom.auth.shared.components.AuthGuestLink
import com.darkzoom.auth.shared.components.AuthLoadingScreen
import com.darkzoom.auth.shared.components.AuthPrimaryButton
import com.darkzoom.auth.shared.components.AuthSocialRow
import com.darkzoom.auth.shared.components.AuthTabRow
import com.darkzoom.auth.shared.components.AuthTextField
import com.darkzoom.auth.login.viewmodel.LoginAction
import com.darkzoom.auth.login.viewmodel.LoginEvent
import com.darkzoom.auth.login.viewmodel.LoginUiState
import com.darkzoom.auth.login.viewmodel.LoginViewModel
import com.example.design_system.theme.AppTheme
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID =
    "187682081726-3uu2n467cah51k3g40e35o1cv4ne0the.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper(WEB_CLIENT_ID) }

    LaunchedEffect(state) {
        if (state is LoginUiState.Success) onLoginSuccess()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.TriggerGoogleSignIn -> {
                    scope.launch {
                        try {
                            val activity = context as Activity
                            val idToken = googleSignInHelper.signIn(activity)
                            viewModel.onAction(LoginAction.GoogleIdTokenReceived(idToken))
                        } catch (e: GetCredentialCancellationException) {
                        } catch (e: Exception) {
                            viewModel.onAction(
                                LoginAction.GoogleSignInFailed(e.message ?: "Google sign-in failed")
                            )
                        }
                    }
                }
            }
        }
    }

    when (val currentState = state) {
        is LoginUiState.Form -> LoginContent(
            state = currentState,
            onAction = viewModel::onAction,
            onNavigateToRegister = onNavigateToRegister,
            onContinueAsGuest = onContinueAsGuest,
            modifier = modifier,
        )

        is LoginUiState.Loading -> AuthLoadingScreen(
            label = stringResource(R.string.auth_login_loading_label),
            modifier = modifier,
        )

        is LoginUiState.Error -> AuthErrorScreen(
            message = currentState.message,
            onRetry = { viewModel.onAction(LoginAction.LoginClicked) },
            onBack = onNavigateToRegister,
            modifier = modifier,
        )

        is LoginUiState.Success -> Unit
    }
}


@Composable
internal fun LoginContent(
    state: LoginUiState.Form,
    onAction: (LoginAction) -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 30.dp, bottom = 40.dp),
    ) {

        Spacer(Modifier.height(34.dp))

        LoginHeadline()

        Spacer(Modifier.height(26.dp))

        AuthTabRow(
            isSignInSelected = true,
            onSignInClick = {},
            onRegisterClick = onNavigateToRegister,
        )

        Spacer(Modifier.height(20.dp))

        AuthTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.EmailChanged(it)) },
            placeholder = stringResource(R.string.auth_login_email_placeholder),
            errorMessage = state.emailError,
            imeAction = ImeAction.Next,
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            placeholder = stringResource(R.string.auth_login_password_placeholder),
            isPassword = true,
            errorMessage = state.passwordError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { onAction(LoginAction.LoginClicked) }
            ),
        )

        Spacer(Modifier.height(22.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_login_button),
            onClick = { onAction(LoginAction.LoginClicked) },
            enabled = state.isSubmitEnabled,
        )

        Spacer(Modifier.height(30.dp))

        AuthSocialRow(
            onGoogleClick = { onAction(LoginAction.GoogleClicked) },
            isGoogleLoading = state.isGoogleLoading,
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(30.dp))

        AuthGuestLink(onClick = onContinueAsGuest)
    }
}


@Composable
private fun LoginHeadline(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.auth_login_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 30.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.9).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.auth_login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp,
        )
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Filled")
@Composable
private fun LoginContentFilledPreview() {
    AppTheme {
        LoginContent(
            state = LoginUiState.Form(email = "user@example.com", password = "password123"),
            onAction = {},
            onNavigateToRegister = {},
            onContinueAsGuest = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Google Loading")
@Composable
private fun LoginContentGoogleLoadingPreview() {
    AppTheme {
        LoginContent(
            state = LoginUiState.Form(isGoogleLoading = true),
            onAction = {},
            onNavigateToRegister = {},
            onContinueAsGuest = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dark")
@Composable
private fun LoginContentDarkPreview() {
    AppTheme(darkTheme = true) {
        LoginContent(
            state = LoginUiState.Form(email = "user@example.com", password = "password123"),
            onAction = {},
            onNavigateToRegister = {},
            onContinueAsGuest = {},
        )
    }
}
