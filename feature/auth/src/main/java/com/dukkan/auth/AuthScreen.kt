package com.dukkan.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.auth.login.view.LoginContent
import com.dukkan.auth.register.view.RegisterContent
import com.dukkan.auth.shared.GoogleSignInHelper
import com.dukkan.auth.shared.components.AuthErrorScreen
import com.dukkan.auth.shared.components.AuthLoadingScreen
import com.dukkan.auth.shared.components.AuthTabRow
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
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        AuthTabRow(
            isSignInSelected = state.isLoginMode,
            onSignInClick = { if (!state.isLoginMode) onAction(AuthAction.ToggleMode) },
            onRegisterClick = { if (state.isLoginMode) onAction(AuthAction.ToggleMode) },
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        AnimatedContent(
            targetState = state.isLoginMode,
            transitionSpec = {
                val animationSpec = tween<IntOffset>(300)
                if (targetState) {
                    slideInHorizontally(
                        animationSpec = animationSpec,
                        initialOffsetX = { fullWidth -> -fullWidth }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = animationSpec,
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    ).using(SizeTransform(clip = false))
                } else {
                    slideInHorizontally(
                        animationSpec = animationSpec,
                        initialOffsetX = { fullWidth -> fullWidth }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = animationSpec,
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    ).using(SizeTransform(clip = false))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = "AuthAnimation"
        ) { showLogin ->
            if (showLogin) {
                LoginContent(
                    state = state,
                    onAction = onAction,
                    onNavigateToRegister = { if (showLogin) onAction(AuthAction.ToggleMode) },
                    onContinueAsGuest = onNavigateToHome,
                )
            } else {
                RegisterContent(
                    state = state,
                    onAction = onAction,
                    onNavigateToLogin = { if (!showLogin) onAction(AuthAction.ToggleMode) },
                    onContinueAsGuest = onNavigateToHome,
                )
            }
        }
    }
}
