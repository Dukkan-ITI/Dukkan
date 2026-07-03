package com.dukkan.shopping_cart.uistate

import com.dukkan.domain.model.CartItem

data class ShoppingCartState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = 0.0,
    val promoCode: String = "",
    val isApplyingPromo: Boolean = false,
    val promoError: String? = null,
    val showRemoveDialogForItem: CartItem? = null
)
