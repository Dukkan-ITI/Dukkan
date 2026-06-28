package com.dukkan.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.navigation.Screen

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

            }
        }

        composable(route = Screen.Search.route) {}

        composable(route = Screen.Favorite.route) {}

        composable(route = Screen.ShoppingCart.route) {}

        composable(route = Screen.Profile.route) {}

        composable(route = Screen.ProductDetail.route) {}
    }
}