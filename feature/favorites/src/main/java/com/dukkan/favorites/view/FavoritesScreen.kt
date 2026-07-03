package com.dukkan.favorites.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.design_system.components.GuestPlaceholderScreen
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.favorites.R
import com.dukkan.favorites.components.EmptyContent
import com.dukkan.favorites.components.FavoriteItem
import com.dukkan.favorites.components.RemoveFavoriteDialog
import com.dukkan.favorites.uistate.FavoritesUiState
import com.dukkan.favorites.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onSignInClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val productToRemove by viewModel.showRemoveDialogForProduct.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        GuestPlaceholderScreen(
            title = stringResource(id = R.string.wishlist_title),
            onSignInClick = onSignInClick,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        when (val state = uiState) {
            is FavoritesUiState.Loading -> CircularProgressIndicator()
            is FavoritesUiState.Empty -> EmptyContent()
            is FavoritesUiState.Success -> FavoritesContent(
                favorites = state.favorites,
                onUnfavClick = { viewModel.showRemoveDialog(it) }
            )
        }
    }

    productToRemove?.let { product ->
        RemoveFavoriteDialog(
            product = product,
            onDismiss = viewModel::dismissRemoveDialog,
            onConfirm = viewModel::confirmRemove
        )
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<FavoriteProduct>,
    onUnfavClick: (FavoriteProduct) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = bottomBarSpace())
    ) {

        item(span = { GridItemSpan(2) }) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.wishlist_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.wishlist_saved_count, favorites.size),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        items(favorites, key = { it.id }) { product ->
            FavoriteItem(
                product = product,
                onUnfav = { onUnfavClick(product) }
            )
        }
    }
}