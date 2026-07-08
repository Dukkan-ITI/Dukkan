package com.dukkan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.toRoute
import com.dukkan.address.view.SavedAddressesScreen
import com.dukkan.auth.view.AuthScreen
import com.dukkan.brands.view.BrandProductsScreen
import com.dukkan.brands.view.BrandsScreen
import com.dukkan.categories.view.CategoriesScreen
import com.dukkan.categories.view.CategoryProductsScreen
import com.dukkan.chatbot.view.ChatScreen
import com.dukkan.favorites.view.FavoritesView
import com.dukkan.home.view.HomeScreen
import com.dukkan.home.view.AllProductsScreen
import com.dukkan.navigation.Screen
import com.dukkan.onboarding.view.OnboardingView
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.paymentNavGraph
import com.dukkan.order_list.view.OrderHistoryScreen
import com.dukkan.search.view.SearchScreen
import com.dukkan.settings.view.ProfileScreen
import com.dukkan.shopping_cart.view.ShoppingCartView
import com.dukkan.product_details.view.ProductDetailsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                onSeeAllClicked = {
                    navController.navigate(Screen.AllProducts)
                },
                onNavigateToCategories = { navController.navigate(Screen.Categories) },
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryProducts(categoryId = category.id, categoryName = category.name))
                },
                onNavigateToBrands = { navController.navigate(Screen.Brands) },
                onBrandClick = { brand ->
                    navController.navigate(Screen.BrandProducts(brand.name))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetail(productId = productId))
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        composable<Screen.Favorite> {
            FavoritesView(
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail(productId = productId))
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        composable<Screen.AllProducts> {
            AllProductsScreen(
                onBackClick = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail(productId = product.id))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                }
            )
        }

        composable<Screen.ShoppingCart> {
            ShoppingCartView(
                onStartShoppingClick = { navController.navigate(Screen.Home) },
                onCheckoutClick = { navController.navigate(Screen.Payment) },
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        paymentNavGraph(
            navController    = navController,
            onPaymentResult = { result ->
                // Here is where other modules are informed of the payment outcome
                when (result) {
                    is PaymentResult.Success -> {
                        if (result.paymentMethod == "CASH") {
                            Toast.makeText(context, "Order placed! You will pay with cash upon delivery.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Payment successful! Order confirmed.", Toast.LENGTH_LONG).show()
                        }
                    }
                    is PaymentResult.Failed -> {
                        // Payment failed! You can show an error or log it.
                        // val reason = result.error
                    }
                    is PaymentResult.Pending -> {
                        // Payment is pending (e.g. waiting for cash at Kiosk).
                    }
                    PaymentResult.Cancelled -> {
                        // The user manually backed out of the payment flow.
                    }
                }

                // After handling the result, navigate the user to the appropriate screen
                navController.navigate(Screen.Home) {
                    popUpTo<Screen.Home> { inclusive = true }
                }
            },
        )

        composable<Screen.Profile> {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                },
                onNavigateToOrderList = {
                    navController.navigate(Screen.OrderHistory)
                },
                onNavigateToSavedAddresses = {
                    navController.navigate(Screen.SavedAddresses)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        composable<Screen.OrderHistory> {
            OrderHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ProductDetail> {
            ProductDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                },
                onCompareClick = { productId, productTitle ->
                    navController.navigate(Screen.CompareProductSearch(baseProductId = productId, baseProductTitle = productTitle))
                }
            )
        }

        composable<Screen.CompareProductSearch> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CompareProductSearch>()
            val searchViewModel: com.dukkan.search.viewmodel.SearchViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            
            androidx.compose.runtime.LaunchedEffect(args.baseProductTitle) {
                if (searchViewModel.uiState.value.queryInput.isEmpty()) {
                    searchViewModel.onQueryInputChanged(args.baseProductTitle)
                    searchViewModel.onSearchSubmitted(args.baseProductTitle)
                }
            }
            
            SearchScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = searchViewModel,
                onNavigateToProductDetails = { selectedProductId ->
                    navController.navigate(Screen.ProductComparison(productId1 = args.baseProductId, productId2 = selectedProductId)) {
                        popUpTo<Screen.CompareProductSearch> { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chatbot)
                }
            )
        }

        composable<Screen.ProductComparison> {
            com.dukkan.product_details.view.ProductComparisonScreen(
                onBackClick = { navController.popBackStack() }
            )
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
                    navController.navigate(Screen.CategoryProducts(categoryId = category.id, categoryName = category.name))
                }
            )
        }

        composable<Screen.CategoryProducts> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CategoryProducts>()
            CategoryProductsScreen(
                categoryId = args.categoryId,
                categoryName = args.categoryName,
                onBackClick = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail(productId = product.id))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                }
            )
        }

        composable<Screen.Brands> {
            BrandsScreen(
                onBackClick = { navController.popBackStack() },
                onBrandClick = { brand ->
                    navController.navigate(Screen.BrandProducts(brand.name))
                }
            )
        }

        composable<Screen.BrandProducts> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.BrandProducts>()
            BrandProductsScreen(
                vendor = args.vendor,
                onBackClick = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail(productId = product.id))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorite)
                }
            )
        }

        composable<Screen.Chatbot> {
            ChatScreen()
        }
    }
}