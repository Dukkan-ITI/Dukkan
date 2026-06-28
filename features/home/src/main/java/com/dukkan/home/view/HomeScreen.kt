package com.dukkan.home.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dukkan.home.components.HomeCategoryList
import com.dukkan.home.components.HomeHeader
import com.dukkan.home.components.HomeProductCard
import com.example.design_system.theme.AppTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "New In", "Clothing", "Shoes")

    AppTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                HomeHeader()

                HomeCategoryList(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(6) { index ->
                        HomeProductCard(
                            title = "Product ${index + 1}",
                            priceLabel = "$49.99"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen()
    }
}