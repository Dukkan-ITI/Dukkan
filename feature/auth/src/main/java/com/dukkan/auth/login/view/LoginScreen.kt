package com.dukkan.auth.login.view

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.auth.R
import com.dukkan.auth.shared.components.AuthGuestLink
import com.dukkan.auth.shared.components.AuthPrimaryButton
import com.dukkan.auth.shared.components.AuthSocialRow
import com.dukkan.auth.shared.components.AuthTextField
import com.dukkan.auth.login.viewmodel.LoginAction
import com.dukkan.auth.login.viewmodel.LoginUiState

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(bottom = 40.dp),
    ) {

        Spacer(Modifier.height(4.dp))

        LoginHeadline()

        Spacer(Modifier.height(30.dp))

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


