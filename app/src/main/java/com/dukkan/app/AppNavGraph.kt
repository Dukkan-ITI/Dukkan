package com.dukkan.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView

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
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(route = Screen.Search.route) {}

        composable(route = Screen.Favorite.route) {}

        composable(route = Screen.ShoppingCart.route) {}

        composable(route = Screen.Profile.route) {}

        composable(route = Screen.ProductDetail.route) {}
    }
}