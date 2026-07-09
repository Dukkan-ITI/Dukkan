package com.dukkan.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.settings.components.LogoutRow
import com.dukkan.settings.components.OrderHistorySection
import com.dukkan.settings.components.ProfileHeader
import com.dukkan.settings.components.SavedAddressesRow
import com.dukkan.settings.components.SettingsCard
import com.dukkan.settings.viewmodel.ProfileState
import com.dukkan.settings.viewmodel.ProfileViewModel
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.design_system.components.ChatFab
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.model.ThemeMode
import androidx.compose.material3.Scaffold

@Composable
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToOrderList: () -> Unit,
    onNavigateToSavedAddresses: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileContent(
        state = state,
        onThemeSelected = viewModel::onThemeSelected,
        onCurrencySelected = viewModel::onCurrencySelected,
        onLanguageSelected = viewModel::onLanguageSelected,
        onSavedAddressesClick = onNavigateToSavedAddresses,
        onSignInClick = onNavigateToAuth,
        onLogoutClick = { viewModel.onLogout(onComplete = onNavigateToAuth) },
        onSeeAllClick = onNavigateToOrderList,
        onNavigateToChat = onNavigateToChat,
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onThemeSelected: (ThemeMode) -> Unit,
    onCurrencySelected: (AppCurrency) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSavedAddressesClick: () -> Unit,
    onSignInClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ChatFab(onClick = onNavigateToChat,
                modifier = Modifier.padding(bottom = bottomBarSpace()))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .padding(bottom = bottomBarSpace(), top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ProfileHeader(
                user = state.user,
                onSignInClick = onSignInClick,
            )

            SettingsCard(
                themeMode = state.themeMode,
                currency = state.currency,
                language = state.language,
                onThemeSelected = onThemeSelected,
                onCurrencySelected = onCurrencySelected,
                onLanguageSelected = onLanguageSelected,
            )

            if (state.isLoggedIn) {
                OrderHistorySection(
                    orders = state.orders,
                    onSeeAllClick = onSeeAllClick,
                    isLoading = state.ordersLoading,
                )

                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    SavedAddressesRow(onClick = onSavedAddressesClick, enabled = state.isOnline)

                    LogoutRow(onClick = onLogoutClick)
                }
            }
        }
    }
}
