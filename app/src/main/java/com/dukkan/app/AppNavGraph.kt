package com.dukkan.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        composable<Screen.Home> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Greeting(name = "to Dukkan")
            }
        }

        composable<Screen.Search> {}

        composable<Screen.Favorite> {}

        composable<Screen.ShoppingCart> {}

        composable<Screen.Profile> {}

        composable<Screen.ProductDetail> { backStackEntry ->
            val productDetail = backStackEntry.toRoute<Screen.ProductDetail>()

//            ProductDetailScreen(productId = productDetail.productId)
        }
    }
}