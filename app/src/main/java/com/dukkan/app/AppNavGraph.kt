package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.shopping_cart.view.ShoppingCartView
import com.msayeh.product_details.view.ProductDetailsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier
    ) {

        composable<Screen.Login> {}

        composable<Screen.SignUp> {}

        composable<Screen.Home> {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetail(productId = productId))
                }
            )
        }

        composable<Screen.Search> {}

        composable<Screen.Favorite> {}

        composable<Screen.Favorite> {
            FavoritesView()
        }

        composable<Screen.ShoppingCart> {
            ShoppingCartView(
                onStartShoppingClick = { navController.navigate(Screen.Home) },
                onCheckoutClick = { }
            )
        }

        composable<Screen.ProductDetail> {
            ProductDetailsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}