package com.dukkan.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Onboarding

    @Serializable
    object Auth
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

    @Serializable
    object Categories

    @Serializable
    data class CategoryProducts(val categoryHandle: String)
}
