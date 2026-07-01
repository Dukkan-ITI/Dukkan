package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dukkan.navigation.*

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

        composable(route = Screen.Home) {
            HomeScreen(
                modifier = Modifier.fillMaxSize()
            )
        }

        composable<Screen.Search> {}

        composable<Screen.Favorite> {}

        composable(route = Screen.Favorite) {
            FavoritesView()
        }

        composable(route = Screen.ShoppingCart) {
            ShoppingCartView(
                onStartShoppingClick = { navController.navigate(Screen.Home) },
                onCheckoutClick = { }
            )
        }

        composable<Screen.ProductDetail> { backStackEntry ->
            val productDetail = backStackEntry.toRoute<Screen.ProductDetail>()

//            ProductDetailScreen(productId = productDetail.productId)
        }
    }
}