package com.darkzoom.auth.register.view

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
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darkzoom.auth.R
import com.darkzoom.auth.register.viewmodel.RegisterEvent
import com.darkzoom.auth.register.viewmodel.RegisterUiState
import com.darkzoom.auth.register.viewmodel.RegisterViewModel
import com.darkzoom.auth.shared.GoogleSignInHelper
import com.darkzoom.auth.shared.components.AuthErrorScreen
import com.darkzoom.auth.shared.components.AuthGuestLink
import com.darkzoom.auth.shared.components.AuthLoadingScreen
import com.darkzoom.auth.shared.components.AuthPrimaryButton
import com.darkzoom.auth.shared.components.AuthSocialRow
import com.darkzoom.auth.shared.components.AuthTabRow
import com.darkzoom.auth.shared.components.AuthTextField
import com.darkzoom.auth.shared.components.AuthVerificationHint
import com.example.design_system.theme.AppTheme
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID =
    "187682081726-3uu2n467cah51k3g40e35o1cv4ne0the.apps.googleusercontent.com"

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper(WEB_CLIENT_ID) }

    LaunchedEffect(state) {
        if (state is RegisterUiState.Success) onRegisterSuccess()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.TriggerGoogleSignIn -> {
                    scope.launch {
                        try {
                            val activity = context as Activity
                            val idToken = googleSignInHelper.signIn(activity)
                            viewModel.onGoogleIdTokenReceived(idToken)
                        } catch (e: GetCredentialCancellationException) {
                        } catch (e: Exception) {
                            viewModel.onGoogleSignInFailed(
                                e.message ?: "Google sign-in failed"
                            )
                        }
                    }
                }
            }
        }
    }

    when (val currentState = state) {
        is RegisterUiState.Form -> RegisterContent(
            state = currentState,
            onNameChange = viewModel::onNameChanged,
            onEmailChange = viewModel::onEmailChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onRegisterClick = viewModel::onRegisterClicked,
            onNavigateToLogin = onNavigateToLogin,
            onGoogleClick = viewModel::onGoogleClicked,
            onContinueAsGuest = onContinueAsGuest,
            modifier = modifier,
        )

        is RegisterUiState.Loading -> AuthLoadingScreen(
            label = stringResource(R.string.auth_register_loading_label),
            modifier = modifier,
        )

        is RegisterUiState.Error -> AuthErrorScreen(
            message = currentState.message,
            onRetry = viewModel::onRegisterClicked,
            onBack = onNavigateToLogin,
            modifier = modifier,
        )

        is RegisterUiState.Success -> Unit
    }
}


@Composable
internal fun RegisterContent(
    state: RegisterUiState.Form,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGoogleClick: () -> Unit,
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

        RegisterHeadline()

        Spacer(Modifier.height(26.dp))

        AuthTabRow(
            isSignInSelected = false,
            onSignInClick = onNavigateToLogin,
            onRegisterClick = {},
        )

        Spacer(Modifier.height(20.dp))

        AuthTextField(
            value = state.name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.auth_register_name_placeholder),
            errorMessage = state.nameError,
            imeAction = ImeAction.Next,
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.auth_register_email_placeholder),
            errorMessage = state.emailError,
            imeAction = ImeAction.Next,
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.auth_register_password_placeholder),
            isPassword = true,
            errorMessage = state.passwordError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { onRegisterClick() }
            ),
        )

        Spacer(Modifier.height(13.dp))

        AuthVerificationHint()

        Spacer(Modifier.height(22.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_register_button),
            onClick = onRegisterClick,
            enabled = state.isSubmitEnabled,
        )

        Spacer(Modifier.height(30.dp))

        AuthSocialRow(
            onGoogleClick = onGoogleClick,
            isGoogleLoading = state.isGoogleLoading,
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(30.dp))

        AuthGuestLink(onClick = onContinueAsGuest)
    }
}


@Composable
private fun RegisterHeadline(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.auth_register_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 30.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.9).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.auth_register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp,
        )
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Filled")
@Composable
private fun RegisterContentFilledPreview() {
    AppTheme {
        RegisterContent(
            state = RegisterUiState.Form(
                name = "John Doe",
                email = "user@example.com",
                password = "password123",
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {},
            onGoogleClick = {},
            onContinueAsGuest = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dark")
@Composable
private fun RegisterContentDarkPreview() {
    AppTheme(darkTheme = true) {
        RegisterContent(
            state = RegisterUiState.Form(
                name = "John Doe",
                email = "user@example.com",
                password = "password123",
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {},
            onGoogleClick = {},
            onContinueAsGuest = {},
        )
    }
}