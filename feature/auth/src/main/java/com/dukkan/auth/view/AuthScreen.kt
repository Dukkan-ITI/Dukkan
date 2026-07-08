package com.dukkan.auth.view

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.auth.R
import com.dukkan.auth.utils.GoogleSignInHelper

import com.dukkan.auth.components.AuthEmailVerificationScreen
import com.dukkan.auth.components.AuthForgotPasswordScreen
import com.dukkan.auth.components.AuthGuestLink
import com.dukkan.auth.components.AuthLoadingScreen
import com.dukkan.auth.components.AuthPrimaryButton
import com.dukkan.auth.components.AuthSocialRow
import com.dukkan.auth.components.AuthTabRow
import com.dukkan.auth.components.AuthTextField
import com.dukkan.auth.viewmodel.AuthAction
import com.dukkan.auth.viewmodel.AuthEvent
import com.dukkan.auth.viewmodel.AuthUiState
import com.dukkan.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by authViewModel.isOnline.collectAsStateWithLifecycle()
    val currentState = authState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper() }
    var rememberedLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(currentState) {
        if (currentState is AuthUiState.Success) onNavigateToHome()
    }

    LaunchedEffect(currentState) {
        if (currentState is AuthUiState.Form) {
            rememberedLoginMode = currentState.isLoginMode
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.TriggerGoogleSignIn -> {
                    scope.launch {
                        try {
                            val activity = context as Activity
                            val idToken = googleSignInHelper.signIn(activity)
                            authViewModel.onAction(AuthAction.GoogleIdTokenReceived(idToken))
                        } catch (e: GetCredentialCancellationException) {
                            // Ignored
                        } catch (e: Exception) {
                            authViewModel.onAction(
                                AuthAction.GoogleSignInFailed(
                                    e.message
                                        ?: context.getString(R.string.auth_error_google_sign_in_failed)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val onAction: (AuthAction) -> Unit = remember(authViewModel) { authViewModel::onAction }

    if (!isOnline) {
        com.dukkan.design_system.components.ErrorScreen(
            title = stringResource(com.dukkan.design_system.R.string.offline_title),
            message = stringResource(R.string.auth_offline_message),
            lottieRawRes = com.dukkan.design_system.R.raw.no_internet,
            onRetry = onNavigateToHome,
            retryText = stringResource(R.string.auth_guest_link_label),
            modifier = modifier
        )
    } else {
        when (val state = currentState) {
            is AuthUiState.Loading -> {
                val labelRes = if (rememberedLoginMode) {
                    R.string.auth_login_loading_label
                } else {
                    R.string.auth_register_loading_label
                }
                AuthLoadingScreen(
                    label = stringResource(labelRes),
                    modifier = modifier
                )
            }

            is AuthUiState.Form -> {
                AuthFormContent(
                    state = state,
                    onAction = onAction,
                    onNavigateToHome = onNavigateToHome,
                    modifier = modifier
                )
            }

            is AuthUiState.EmailVerificationPending -> {
                AuthEmailVerificationScreen(
                    email = state.email,
                    isResending = state.isResending,
                    isChecking = state.isChecking,
                    resendCooldownSeconds = state.resendCooldownSeconds,
                    infoMessage = state.infoMessage,
                    onResendClick = { onAction(AuthAction.ResendVerificationClicked) },
                    onCheckClick = { onAction(AuthAction.CheckVerificationClicked) },
                    onBackToLoginClick = { onAction(AuthAction.BackToLoginClicked) },
                    modifier = modifier
                )
            }

            is AuthUiState.ForgotPassword -> {
                AuthForgotPasswordScreen(
                    email = state.email,
                    isLoading = state.isLoading,
                    emailError = state.emailError,
                    isEmailSent = state.isEmailSent,
                    onEmailChanged = { onAction(AuthAction.ForgotPasswordEmailChanged(it)) },
                    onSendClick = { onAction(AuthAction.SendResetLinkClicked) },
                    onBackToLoginClick = { onAction(AuthAction.BackToLoginFromForgotPasswordClicked) },
                    modifier = modifier
                )
            }

            is AuthUiState.Success -> Unit
        }
    }
}

@Composable
private fun AuthFormContent(
    state: AuthUiState.Form,
    onAction: (AuthAction) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val logoRes = if (isRtl) {
            com.dukkan.design_system.R.drawable.logo_ar
        } else {
            com.dukkan.design_system.R.drawable.logo_en
        }

        Image(
            painter = painterResource(id = logoRes),
            contentDescription = null,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )


        AuthHeadline(
            title = if (state.isLoginMode) stringResource(R.string.auth_login_title) else stringResource(
                R.string.auth_register_title
            ),
            subtitle = if (state.isLoginMode) stringResource(R.string.auth_login_subtitle) else stringResource(
                R.string.auth_register_subtitle
            ),
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        AuthTabRow(
            isSignInSelected = state.isLoginMode,
            onSignInClick = { if (!state.isLoginMode) onAction(AuthAction.ToggleMode) },
            onRegisterClick = { if (state.isLoginMode) onAction(AuthAction.ToggleMode) },
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp)
        ) {
            val animationSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)

            AnimatedVisibility(
                visible = !state.isLoginMode,
                enter = fadeIn(animationSpec = animationSpec) +
                        expandVertically(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            )
                        ),
                exit = fadeOut(animationSpec = animationSpec) +
                        shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            )
                        )
            ) {
                Column {
                    AuthTextField(
                        value = state.firstName,
                        onValueChange = { onAction(AuthAction.FirstNameChanged(it)) },
                        placeholder = stringResource(R.string.auth_register_first_name_placeholder),
                        errorMessage = state.firstNameError,
                        imeAction = ImeAction.Next,
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = state.lastName,
                        onValueChange = { onAction(AuthAction.LastNameChanged(it)) },
                        placeholder = stringResource(R.string.auth_register_last_name_placeholder),
                        errorMessage = state.lastNameError,
                        imeAction = ImeAction.Next,
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            AuthTextField(
                value = state.email,
                onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                placeholder = stringResource(if (state.isLoginMode) R.string.auth_login_email_placeholder else R.string.auth_register_email_placeholder),
                errorMessage = state.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = state.password,
                onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                placeholder = stringResource(if (state.isLoginMode) R.string.auth_login_password_placeholder else R.string.auth_register_password_placeholder),
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onTogglePasswordVisibility = { onAction(AuthAction.TogglePasswordVisibility) },
                errorMessage = state.passwordError,
                imeAction = if (state.isLoginMode) ImeAction.Done else ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onDone = { if (state.isLoginMode) onAction(AuthAction.SubmitClicked) }
                ),
            )

            AnimatedVisibility(visible = state.isLoginMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onAction(AuthAction.ForgotPasswordClicked) },
                        contentPadding = PaddingValues(top = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_forgot_password_link),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !state.isLoginMode,
                enter = fadeIn(animationSpec = animationSpec) +
                        expandVertically(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            )
                        ),
                exit = fadeOut(animationSpec = animationSpec) +
                        shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            )
                        )
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = state.confirmPassword,
                        onValueChange = { onAction(AuthAction.ConfirmPasswordChanged(it)) },
                        placeholder = stringResource(R.string.auth_register_confirm_password_placeholder),
                        isPassword = true,
                        isPasswordVisible = state.isConfirmPasswordVisible,
                        onTogglePasswordVisibility = { onAction(AuthAction.ToggleConfirmPasswordVisibility) },
                        errorMessage = state.confirmPasswordError,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { onAction(AuthAction.SubmitClicked) }
                        ),
                    )
                    Spacer(Modifier.height(1.dp))
                }
            }

            Spacer(Modifier.height(22.dp))

            AuthPrimaryButton(
                text = if (state.isLoginMode) stringResource(R.string.auth_login_button) else stringResource(
                    R.string.auth_register_button
                ),
                onClick = { onAction(AuthAction.SubmitClicked) },
                enabled = state.isSubmitEnabled,
            )

            Spacer(Modifier.height(30.dp))

            AuthSocialRow(
                onGoogleClick = { onAction(AuthAction.GoogleClicked) },
                isGoogleLoading = state.isGoogleLoading,
            )

            Spacer(Modifier.height(30.dp))

            AuthGuestLink(onClick = onNavigateToHome)
        }
    }
}

@Composable
private fun AuthHeadline(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = title,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350)) using SizeTransform(
                    clip = false
                )
            },
            label = "TitleAnimation"
        ) { targetTitle ->
            Text(
                text = targetTitle,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1).sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(4.dp))
        AnimatedContent(
            targetState = subtitle,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350)) using SizeTransform(
                    clip = false
                )
            },
            label = "SubtitleAnimation"
        ) { targetSubtitle ->
            Text(
                text = targetSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
            )
        }
    }
}