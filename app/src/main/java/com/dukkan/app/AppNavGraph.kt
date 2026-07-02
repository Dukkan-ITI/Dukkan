package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dukkan.auth.view.AuthScreen
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView
import com.dukkan.settings.view.ProfileScreen
import com.dukkan.shopping_cart.view.ShoppingCartView
import com.dukkan.search.view.SearchScreen
import com.example.design_system.components.PlaceholderScreen
import com.msayeh.product_details.view.ProductDetailsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth,
        modifier = modifier
    ) {

        composable<Screen.Login> {}

        composable<Screen.Onboarding> {
            OnboardingView(
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
            FavoritesView()
        }

        composable<Screen.ShoppingCart> {
            ShoppingCartView(
                onStartShoppingClick = { navController.navigate(Screen.Home) },
                onCheckoutClick = { }
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
                    // TODO: Navigate to order list screen when it's ready
                }
            )
        }

        composable<Screen.ProductDetail> {
            ProductDetailsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}