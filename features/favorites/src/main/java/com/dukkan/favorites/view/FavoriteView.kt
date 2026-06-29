package com.dukkan.favorites.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dukkan.favorites.components.EmptyContent
import com.dukkan.favorites.components.FavoritesContent
import com.dukkan.favorites.uistate.FavoritesUiState
import com.dukkan.favorites.viewmodel.FavoritesViewModel

@Composable
fun FavoritesView(
    viewModel: FavoritesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        when (val state = uiState) {
            is FavoritesUiState.Loading->CircularProgressIndicator()
            is FavoritesUiState.Empty   -> EmptyContent()
            is FavoritesUiState.Success -> FavoritesContent(
                favorites = state.favorites,
                onUnfav = viewModel::onUnfav
            )
        }
    }
}

