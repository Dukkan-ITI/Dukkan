package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.auth.view.AuthScreen
import com.dukkan.favorites.view.FavoritesScreen
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingScreen
import com.dukkan.product_details.view.ProductDetailsScreen
import com.dukkan.search.view.SearchScreen
import com.dukkan.settings.view.ProfileScreen
import com.dukkan.shopping_cart.view.ShoppingCartScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable<Screen.Login> {}

        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Auth> {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.SignUp> {}

        composable<Screen.Home> {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetail(productId = productId))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search)
                }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetail(productId = productId))
                }
            )
        }

        composable<Screen.Favorite> {
            FavoritesScreen(
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ShoppingCart> {
            ShoppingCartScreen(
                onStartShoppingClick = { navController.navigate(Screen.Home) },
                onCheckoutClick = { },
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onNavigateToFavorites = { navController.navigate(Screen.Favorite) },
                onNavigateToOrderList = {
                }
            )
        }

        composable<Screen.ProductDetail> {
            ProductDetailsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}