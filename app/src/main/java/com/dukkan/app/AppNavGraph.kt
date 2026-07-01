package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.home.view.HomeScreen
import com.dukkan.auth.AuthScreen
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView
import com.dukkan.shopping_cart.view.ShoppingCartView


@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route,
        modifier = modifier
    ) {

        composable(route = Screen.Onboarding.route) {
            OnboardingView {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }

        composable(route = Screen.Onboarding.route) {
            OnboardingView(
                onNavigateToLogin = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Auth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(route = Screen.Search.route) {}

        composable(route = Screen.Favorite.route) {
            FavoritesView()
        }

        composable(route = Screen.ShoppingCart.route) {
            ShoppingCartView(
                onStartShoppingClick = { navController.navigate(Screen.Home.route) },
                onCheckoutClick = { }
            )
        }

        composable(route = Screen.Profile.route) {}

        composable(route = Screen.ProductDetail.route) {}
    }
}