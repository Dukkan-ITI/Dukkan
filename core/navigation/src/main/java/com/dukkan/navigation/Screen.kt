package com.dukkan.navigation
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object SignUp : Screen("sign_up_screen")
    object Home : Screen("home_screen")
    object Search : Screen("search_screen")
    object Favorite : Screen("favorite_screen")
    object ShoppingCart : Screen("shopping_cart_screen")
    object Profile : Screen("profile_screen")

    object ProductDetail : Screen("product_detail_screen/{productId}") {
        fun createRoute(productId: String) = "product_detail_screen/$productId"
    }
}