package com.dukkan.auth

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.auth.login.view.LoginContent
import com.dukkan.auth.login.viewmodel.LoginAction
import com.dukkan.auth.login.viewmodel.LoginEvent
import com.dukkan.auth.login.viewmodel.LoginUiState
import com.dukkan.auth.login.viewmodel.LoginViewModel
import com.dukkan.auth.register.view.RegisterContent
import com.dukkan.auth.register.viewmodel.RegisterAction
import com.dukkan.auth.register.viewmodel.RegisterEvent
import com.dukkan.auth.register.viewmodel.RegisterUiState
import com.dukkan.auth.register.viewmodel.RegisterViewModel
import com.dukkan.auth.shared.GoogleSignInHelper
import com.dukkan.auth.shared.components.AuthErrorScreen
import com.dukkan.auth.shared.components.AuthLoadingScreen
import com.dukkan.auth.shared.components.AuthTabRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()

    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val registerState by registerViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper() }
    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) onNavigateToHome()
    }

    LaunchedEffect(registerState) {
        if (registerState is RegisterUiState.Success) onNavigateToHome()
    }

    LaunchedEffect(Unit) {
        loginViewModel.events.collect { event ->
            when (event) {
                LoginEvent.TriggerGoogleSignIn -> {
                    scope.launch {
                        try {
                            val activity = context as Activity
                            val idToken = googleSignInHelper.signIn(activity)
                            loginViewModel.onAction(LoginAction.GoogleIdTokenReceived(idToken))
                        } catch (e: GetCredentialCancellationException) {
                        } catch (e: Exception) {
                            loginViewModel.onAction(
                                LoginAction.GoogleSignInFailed(e.message ?: "Google sign-in failed")
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        registerViewModel.events.collect { event ->
            when (event) {
                RegisterEvent.TriggerGoogleSignIn -> {
                    scope.launch {
                        try {
                            val activity = context as Activity
                            val idToken = googleSignInHelper.signIn(activity)
                            registerViewModel.onAction(RegisterAction.GoogleIdTokenReceived(idToken))
                        } catch (e: GetCredentialCancellationException) {
                            // Ignored
                        } catch (e: Exception) {
                            registerViewModel.onAction(
                                RegisterAction.GoogleSignInFailed(e.message ?: "Google sign-in failed")
                            )
                        }
                    }
                }
            }
        }
    }

    if (loginState is LoginUiState.Loading || registerState is RegisterUiState.Loading) {
        val labelRes = if (loginState is LoginUiState.Loading) {
            R.string.auth_login_loading_label
        } else {
            R.string.auth_register_loading_label
        }
        AuthLoadingScreen(
            label = stringResource(labelRes),
            modifier = modifier
        )
        return
    }

    if (loginState is LoginUiState.Error) {
        AuthErrorScreen(
            message = (loginState as LoginUiState.Error).message,
            onRetry = { loginViewModel.onAction(LoginAction.LoginClicked) },
            onBack = { scope.launch { pagerState.animateScrollToPage(1) } },
            modifier = modifier
        )
        return
    }

    if (registerState is RegisterUiState.Error) {
        AuthErrorScreen(
            message = (registerState as RegisterUiState.Error).message,
            onRetry = { registerViewModel.onAction(RegisterAction.RegisterClicked) },
            onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(30.dp))
        
        AuthTabRow(
            isSignInSelected = pagerState.currentPage == 0,
            onSignInClick = { scope.launch { pagerState.animateScrollToPage(0) } },
            onRegisterClick = { scope.launch { pagerState.animateScrollToPage(1) } },
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
                    val formState = loginState as? LoginUiState.Form ?: LoginUiState.Form()
                    LoginContent(
                        state = formState,
                        onAction = loginViewModel::onAction,
                        onNavigateToRegister = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onContinueAsGuest = onNavigateToHome,
                    )
                }
                1 -> {
                    val formState = registerState as? RegisterUiState.Form ?: RegisterUiState.Form()
                    RegisterContent(
                        state = formState,
                        onAction = registerViewModel::onAction,
                        onNavigateToLogin = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onContinueAsGuest = onNavigateToHome,
                    )
                }
            }
        }
    }
}
