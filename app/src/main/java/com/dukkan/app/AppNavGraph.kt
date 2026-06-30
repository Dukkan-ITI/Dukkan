package com.dukkan.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.favorites.viewmodel.FavoritesViewModel
import com.dukkan.navigation.Screen
import com.msayeh.product_details.view.ProductDetailsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        composable(route = Screen.Login.route) {}

        composable(route = Screen.SignUp.route) {}

        composable(route = Screen.Home.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ElevatedButton(onClick = { navController.navigate(Screen.ProductDetail.createRoute("15789895385374")) }) {
                    Text("Navigate to Product Details")
                }
            }
        }

        composable(route = Screen.Search.route) {}

        composable(route = Screen.Favorite.route) {
            val viewModel: FavoritesViewModel = hiltViewModel()
            FavoritesView(viewModel = viewModel)
        }

        composable(route = Screen.ShoppingCart.route) {}

        composable(route = Screen.Profile.route) {}

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                type = NavType.StringType
            })
        ) {
            ProductDetailsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}