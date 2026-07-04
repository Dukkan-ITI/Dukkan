package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dukkan.address.view.SavedAddressesScreen
import com.dukkan.auth.view.AuthScreen
import com.dukkan.categories.view.CategoriesScreen
import com.dukkan.categories.view.CategoryProductsScreen
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.home.view.HomeScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView
import com.dukkan.search.view.SearchScreen
import com.dukkan.settings.view.ProfileScreen
import com.dukkan.shopping_cart.view.ShoppingCartView
import com.dukkan.search.view.SearchScreen
import com.dukkan.product_details.view.ProductDetailsScreen

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
                },
                onNavigateToCategories = { navController.navigate(Screen.Categories) },
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryProducts(category.handle))
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
            FavoritesView(
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ShoppingCart> {
            ShoppingCartView(
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
                },
                onNavigateToSavedAddresses = { navController.navigate(Screen.SavedAddresses) }
            )
        }

        composable<Screen.ProductDetail> {
            ProductDetailsScreen(onBackClick = { navController.popBackStack() })
        }

        composable<Screen.SavedAddresses> {
            SavedAddressesScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { navController.navigate(Screen.Auth) },
            )
        }

        composable<Screen.Categories> {
            CategoriesScreen(
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryProducts(category.handle))
                }
            )
        }

        composable<Screen.CategoryProducts> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CategoryProducts>()
            CategoryProductsScreen(
                categoryHandle = args.categoryHandle,
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail(productId = product.id))
                }
            )
        }
    }
}