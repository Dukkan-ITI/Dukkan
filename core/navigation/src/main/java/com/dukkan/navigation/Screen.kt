package com.dukkan.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Login

    @Serializable
    object SignUp

    @Serializable
    object Home

    @Serializable
    object Search

    @Serializable
    object Favorite

    @Serializable
    object ShoppingCart

    @Serializable
    object Profile

    @Serializable
    data class ProductDetail(val productId: String)

    @Serializable
    object AllProducts
}
