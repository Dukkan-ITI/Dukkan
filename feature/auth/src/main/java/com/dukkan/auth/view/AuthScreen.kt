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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.auth.R
import com.dukkan.auth.utils.GoogleSignInHelper
import com.dukkan.auth.components.AuthErrorScreen
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
                                AuthAction.GoogleSignInFailed(e.message ?: "Google sign-in failed")
                            )
                        }
                    }
                }
            }
        }
    }

    val onAction: (AuthAction) -> Unit = remember(authViewModel) { authViewModel::onAction }

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

        is AuthUiState.Error -> {
            AuthErrorScreen(
                message = state.message,
                onRetry = { onAction(AuthAction.SubmitClicked) },
                onBack = { onAction(AuthAction.ToggleMode) },
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

        is AuthUiState.Success -> Unit
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
            .padding(top = 40.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        AuthHeadline(
            title = if (state.isLoginMode) stringResource(R.string.auth_login_title) else stringResource(
                R.string.auth_register_title),
            subtitle = if (state.isLoginMode) stringResource(R.string.auth_login_subtitle) else stringResource(
                R.string.auth_register_subtitle),
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
                        expandVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = animationSpec) + 
                       shrinkVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            ) {
                Column {
                    AuthTextField(
                        value = state.name,
                        onValueChange = { onAction(AuthAction.NameChanged(it)) },
                        placeholder = stringResource(R.string.auth_register_name_placeholder),
                        errorMessage = state.nameError,
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
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = state.password,
                onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                placeholder = stringResource(if (state.isLoginMode) R.string.auth_login_password_placeholder else R.string.auth_register_password_placeholder),
                isPassword = true,
                errorMessage = state.passwordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { onAction(AuthAction.SubmitClicked) }
                ),
            )

            AnimatedVisibility(
                visible = !state.isLoginMode,
                enter = fadeIn(animationSpec = animationSpec) + 
                        expandVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = animationSpec) + 
                       shrinkVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            ) {
                Column {
                    Spacer(Modifier.height(13.dp))
                }
            }

            Spacer(Modifier.height(22.dp))

            AuthPrimaryButton(
                text = if (state.isLoginMode) stringResource(R.string.auth_login_button) else stringResource(
                    R.string.auth_register_button),
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
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350)) using SizeTransform(clip = false)
            },
            label = "TitleAnimation"
        ) { targetTitle ->
            Text(
                text = targetTitle,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.9).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(8.dp))
        AnimatedContent(
            targetState = subtitle,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350)) using SizeTransform(clip = false)
            },
            label = "SubtitleAnimation"
        ) { targetSubtitle ->
            Text(
                text = targetSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp,
            )
        }
    }
}

