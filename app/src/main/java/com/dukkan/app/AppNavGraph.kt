package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.favorites.viewmodel.FavoritesViewModel
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView
import com.msayeh.product_details.view.ProductDetailsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route,
        modifier = modifier
    ) {

        composable(route = Screen.Onboarding.route) {
            OnboardingView {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }

        composable(route = Screen.Login.route) {}

        composable(route = Screen.SignUp.route) {}

        composable(route = Screen.Home.route) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToProductDetails = { productId ->
                    navController.navigate(
                        Screen.ProductDetail.createRoute(
                            productId.split(
                                "/"
                            ).last()
                        )
                    )
                }
            )
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