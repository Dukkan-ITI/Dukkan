package com.dukkan.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.dukkan.auth.login.view.LoginScreen
import com.dukkan.auth.register.view.RegisterScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
                    LoginScreen(
                        onNavigateToRegister = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onLoginSuccess = onNavigateToHome,
                        onContinueAsGuest = onNavigateToHome
                    )
                }
                1 -> {
                    RegisterScreen(
                        onNavigateToLogin = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onRegisterSuccess = onNavigateToHome,
                        onContinueAsGuest = onNavigateToHome
                    )
                }
            }
        }
    }
}
