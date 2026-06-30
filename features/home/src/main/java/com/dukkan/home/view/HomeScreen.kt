package com.dukkan.home.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import com.dukkan.home.components.*
import com.dukkan.home.viewmodel.HomeViewModel
import com.example.design_system.theme.AppTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeHeader()
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeSearchBar()
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeBanner()
            }

            homeProductSection(
                onFavoriteClick = { product, isFavorite ->
                    viewModel.onFavoriteClick(product, isFavorite)
                }
            )
        }
    }
}