package com.darkzoom.auth.login.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkzoom.auth.login.viewmodel.LoginUiState
import com.darkzoom.auth.login.viewmodel.LoginViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darkzoom.auth.R
import com.darkzoom.auth.shared.components.AuthErrorScreen
import com.darkzoom.auth.shared.components.AuthGuestLink
import com.darkzoom.auth.shared.components.AuthLoadingScreen
import com.darkzoom.auth.shared.components.AuthPrimaryButton
import com.darkzoom.auth.shared.components.AuthSocialRow
import com.darkzoom.auth.shared.components.AuthTabRow
import com.darkzoom.auth.shared.components.AuthTextField
import com.example.design_system.theme.AppTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is LoginUiState.Success) onLoginSuccess()
    }

    when (val currentState = state) {
        is LoginUiState.Form -> LoginContent(
            state = currentState,
            onEmailChange = viewModel::onEmailChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onLoginClick = viewModel::onLoginClicked,
            onNavigateToRegister = onNavigateToRegister,
            onGoogleClick = viewModel::onGoogleClicked,
            onAppleClick = viewModel::onAppleClicked,
            onContinueAsGuest = onContinueAsGuest,
            modifier = modifier,
        )

        is LoginUiState.Loading -> AuthLoadingScreen(
            label = stringResource(R.string.auth_login_loading_label),
            modifier = modifier,
        )

        is LoginUiState.Error -> AuthErrorScreen(
            message = currentState.message,
            onRetry = viewModel::onLoginClicked,
            onBack = onNavigateToRegister,
            modifier = modifier,
        )

        is LoginUiState.Success -> Unit
    }
}


@Composable
internal fun LoginContent(
    state: LoginUiState.Form,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 30.dp, bottom = 40.dp),
    ) {

        Spacer(Modifier.height(34.dp))

        LoginHeadline()

        Spacer(Modifier.height(26.dp))

        AuthTabRow(
            isSignInSelected = true,
            onSignInClick = {  },
            onRegisterClick = onNavigateToRegister,
        )

        Spacer(Modifier.height(20.dp))

        AuthTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.auth_login_email_placeholder),
            errorMessage = state.emailError,
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.auth_login_password_placeholder),
            isPassword = true,
            errorMessage = state.passwordError,
        )

        Spacer(Modifier.height(22.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_login_button),
            onClick = onLoginClick,
            enabled = state.isSubmitEnabled,
        )


        Spacer(Modifier.height(30.dp))

        AuthSocialRow(
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick,
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
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onGoogleClick = {},
            onAppleClick = {},
            onContinueAsGuest = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Empty")
@Composable
private fun LoginContentEmptyPreview() {
    AppTheme {
        LoginContent(
            state = LoginUiState.Form(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onGoogleClick = {},
            onAppleClick = {},
            onContinueAsGuest = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "With errors")
@Composable
private fun LoginContentErrorsPreview() {
    AppTheme {
        LoginContent(
            state = LoginUiState.Form(
                email = "bad-email",
                emailError = "Enter a valid email address",
                password = "123",
                passwordError = "Password must be at least 6 characters",
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onGoogleClick = {},
            onAppleClick = {},
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
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onGoogleClick = {},
            onAppleClick = {},
            onContinueAsGuest = {},
        )
    }
}
